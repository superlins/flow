 package com.zwtech.flow.connector.filter;

 import com.zwtech.flow.connector.ExecutionEnvelope;
 import org.springframework.core.Ordered;
 import reactor.core.publisher.Mono;

 /**
  * @author renc
  */
 public class OrderedConnectorFilter implements ConnectorFilter, Ordered {

     private final ConnectorFilter delegate;

     private final int order;

     public OrderedConnectorFilter(ConnectorFilter delegate, int order) {
         this.delegate = delegate;
         this.order = order;
     }

     public ConnectorFilter getDelegate() {
         return delegate;
     }

     @Override
     public int getOrder() {
         return this.order;
     }

     @Override
     public String toString() {
         return "OrderedConnectorFilter{" + "delegate=" + delegate + ", order=" + order + '}';
     }

     @Override
     public Mono<ExecutionEnvelope> filter(ExecutionEnvelope envelope, ConnectorFilterChain chain) {
         // omit setter
         return this.delegate.filter(envelope, chain).doOnNext(responseSpec -> {

         });
     }
 }
