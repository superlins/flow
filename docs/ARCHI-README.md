# Archi

<!-- TOC -->
* [Archi](#archi)
  * [职能](#职能)
    * [ApiService](#apiservice)
    * [ApiDatasource](#apidatasource)
      * [ExecutionExchange](#executionexchange)
      * [ExecutionEnvelope](#executionenvelope)
      * [RequestSpec](#requestspec)
      * [ResponseSpec](#responsespec)
      * [Connector](#connector)
      * [ConnectorFactory](#connectorfactory)
      * [ConnectorFilter](#connectorfilter)
      * [ConnectorFilterChain](#connectorfilterchain)
  * [模式](#模式)
    * [基于函数式调用模型](#基于函数式调用模型)
    * [基于共享上下文调用模型](#基于共享上下文调用模型)
<!-- TOC -->

## 职能

```text
ServerHttpRequest
+ ServiceInputSchema
   ↓
ServiceBinder
   ↓
Service.ExecutionContext.input
+ Service.DerivedContext
+ DatasourceInputSchema
   ↓
DatasourceBinder
   ↓
Datasource.ExecutionContext.input
   ↓ (ConnectorAdapter)
xxRequestSpec
   ↓
ExecutionEnvelope
   ↓
FilterChain
   ↓
Connector
   ↓ (ConnectorAdapter)
xxResponseSpec
+ DatasourceOutputSchema
   ↓
DatasourceBinder
   ↓
Datasource.ExecutionContext.output
+ Datasource.DerivedContext
   ↓
ServiceContext.output
```

### ApiService

```java
public interface ExecutionContext {
    Optional<JsonNode> input();
    Optional<JsonNode> output();
    Optional<DerivedContext> derived();
}

public interface DerivedContext {
    JsonNode get(String path);
    boolean contains(String path);
    DerivedContext with(String path, JsonNode value);
}
```

### ApiDatasource

#### ExecutionExchange

不可变上下文，是 “执行事实世界”，不参与执行管道

```java
public interface ExecutionExchange {
    ExecutionContext context();
    ExecutionExchange mutate(UnaryOperator<ExecutionContext> operator);
    <T> Optional<T> getAttribute(ExchangeAttributeKey<T> key);
    ExecutionExchange withAttribute(ExchangeAttributeKey<?> key, Object value);
}
```

#### ExecutionEnvelope

不可变上下文， 是 “执行进行时世界”，提供 filter 数据支持

```java
public interface ExecutionEnvelope<REQ, RESP> {
    REQ requestSpec();
    ExecutionEnvelope<REQ, RESP> withRequestSpec(REQ request);
    Optional<RESP> responseSpec();
    ExecutionEnvelope<REQ, RESP> withResponseSpec(RESP response);
    ExecutionAttributes attributes();
}
```

#### RequestSpec

不可变的请求对象，connector 统一输入模型

#### ResponseSpec

不可变得的响应对象，connector 统一输出模型

#### Connector

数据源连接器抽象，轻量、无状态

#### ConnectorFactory

数据源连接器抽象工程，适配连接器的客户端缓存

#### ConnectorFilter

数据源过滤器抽象

```java
public interface ConnectorFilter<REQ, RESP> {
    Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope, ConnectorFilterChain chain);
}
```

#### ConnectorFilterChain

```java
public interface ConnectorFilterChain {
    <REQ, RESP> Mono<ExecutionEnvelope<REQ, RESP>> filter(ExecutionEnvelope<REQ, RESP> envelope);
}
```


## 模式

### 基于函数式调用模型

```java
Mono<ResponseSpec> filter(RequestSpec spec, Chain chain);
```

执行顺序：

```text
filterA.before (specA)
  → filterB.before (specB)
    → filterC.before (specC)
      → connector.execute(specC)
    ← filterC.after (resp)
  ← filterB.after (resp)
← filterA.after (resp)
```

在 filterA.after 阶段，我们无法判断其拿到的 httpRequestSpec，和 connector 实际使用的 specC，是不是同一个。 
specX 是当前调用栈这一层的局部变量，filterB、filterC 一定会新建自己的 RequestSpec，并把它们传给更深一层。但这些新的 RequestSpec，
不会回传给 filterA，同样不会覆盖 filterA 持有的 specA，所以在 filterA.after 里只能看到 specA。

即 filterA 看到的是 “我当初构造的请求” 而不是 “最终被发出去的请求”，这种设计 “牺牲 after 阶段对最终请求形态的感知能力”

### 基于共享上下文调用模型

```java
Mono<Void> filter(ExecutionExchange exchange, Chain chain);
```

请求唯一的 ExecutionExchange 可变对象 “事实演进”：

1.	用户输入（事实 0）
2.	filterA 补充（事实 1）
3.	filterB 改写（事实 2）
4.	filterC 路由 / 规范化（事实 3）
5.	Connector 实际执行（最终事实）

只有第 5 步，才是 “真实发生的事实”，而前面每一步，只是对 “将要发生的事实” 的一次修订

Exchange 模型不是让 “靠前 filter 看到靠后 filter 的内部变量”，而是让所有 filter 面对的是 “同一个逻辑上的请求实体”，只是这个实体在不断被修订

