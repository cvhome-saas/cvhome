package com.asrevo.cvhome.controlplane.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

import com.asrevo.cvhome.commons.domain.Identifier;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.PodId;

@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    public JdbcConfig() {
    }

    @Override
    protected List<?> userConverters() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new Converter<Identifier, String>() {
            @Override
            public String convert(Identifier source) {
                return source.getId().toString();
            }
        });
        converters.add(new Converter<String, ManagerOrgId>() {
            @Override
            public ManagerOrgId convert(String source) {
                return new ManagerOrgId(source);
            }
        });
        converters.add(new Converter<Identifier, String>() {
            @Override
            public String convert(Identifier source) {
                return source.getId().toString();
            }
        });
        converters.add(new Converter<String, ManagerOrgId>() {
            @Override
            public ManagerOrgId convert(String source) {
                return new ManagerOrgId(source);
            }
        });
        converters.add(new Converter<String, ManagerStoreId>() {
            @Override
            public ManagerStoreId convert(String source) {
                return new ManagerStoreId(source);
            }
        });

        converters.add(new Converter<String, PodId>() {
            @Override
            public PodId convert(String source) {
                return new PodId(source);
            }
        });

        return converters;
    }

}
