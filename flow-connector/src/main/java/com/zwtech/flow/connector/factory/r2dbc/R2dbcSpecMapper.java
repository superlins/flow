package com.zwtech.flow.connector.factory.r2dbc;

import org.example.core.server.ServerUpstreamRequest;
import org.springframework.core.convert.converter.Converter;

import java.util.List;

public class R2dbcSpecMapper implements Converter<ServerUpstreamRequest, R2dbcRequestSpec> {

    @Override
    public R2dbcRequestSpec convert(ServerUpstreamRequest request) {

        String sql = (String) request.getGeneralParameters().get("sql");
        List<Object> params = request.getSqlParameters();

        R2dbcRequestSpec spec = new R2dbcRequestSpec().sql(sql);
        for (Object param : params) {
            spec.param(param);
        }
        return spec;
    }
}