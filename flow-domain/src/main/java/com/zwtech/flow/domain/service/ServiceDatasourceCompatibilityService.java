package com.zwtech.flow.domain.service;

import com.zwtech.flow.domain.model.apidatasource.DatasourceContract;
import com.zwtech.flow.domain.model.apiservice.BindingSpec;
import com.zwtech.flow.domain.model.apiservice.ServiceContract;

public interface ServiceDatasourceCompatibilityService {

  /**
   * 业务规则 S-2：
   * ApiService 契约 + BindingSpec 必须满足 Datasource 契约
   */
  void assertCompatible(
      ServiceContract serviceContract,
      BindingSpec bindingSpec,
      DatasourceContract datasourceContract
  );
}