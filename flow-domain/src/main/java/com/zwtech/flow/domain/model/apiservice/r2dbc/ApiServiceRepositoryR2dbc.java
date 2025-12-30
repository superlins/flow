package com.zwtech.flow.domain.model.apiservice.r2dbc;

import com.zwtech.flow.domain.model.apiservice.ApiService;
import com.zwtech.flow.domain.model.apiservice.ApiServiceRepository;
import com.zwtech.flow.domain.model.apiservice.ServiceId;
import reactor.core.publisher.Mono;

/**
 * @author renc
 */
public class ApiServiceRepositoryR2dbc implements ApiServiceRepository {
    @Override
    public Mono<ApiService> find(ServiceId id) {
        return null;
    }

    @Override
    public Mono<Void> save(ApiService service) {
        return null;
    }
}
