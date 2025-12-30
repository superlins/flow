package com.zwtech.flow.domain.model.apiservice;

import reactor.core.publisher.Mono;

public interface ApiServiceRepository {
  Mono<ApiService> find(ServiceId id);
  Mono<Void> save(ApiService service);
}