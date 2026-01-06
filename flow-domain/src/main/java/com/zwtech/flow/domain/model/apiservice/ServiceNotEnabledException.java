package com.zwtech.flow.domain.model.apiservice;

import com.zwtech.flow.domain.shared.DomainException;

/**
 * Service 未启用异常
 *
 * @author renc
 */
public class ServiceNotEnabledException extends DomainException {
    
    public ServiceNotEnabledException(ServiceId id) {
        super("Service " + id + " is not enabled");
    }
}

