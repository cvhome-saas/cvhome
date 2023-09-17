package com.asrevo.cvhome.certificatemanager.config;

import com.asrevo.cvhome.certificatemanager.config.converters.JsonToMapConverter;
import com.asrevo.cvhome.certificatemanager.config.converters.MapToJsonConverter;
import com.asrevo.cvhome.commons.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;
import org.springframework.data.relational.core.mapping.event.AfterSaveCallback;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class JdbcConfig extends AbstractJdbcConfiguration {

    private final ObjectMapper ployJson;

    public JdbcConfig() {
        this.ployJson = JacksonConfig.getPloyJson();
    }

    @Override
    public JdbcCustomConversions jdbcCustomConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new MapToJsonConverter(ployJson));
        converters.add(new JsonToMapConverter(ployJson));
        converters.add(new Converter<Identifier, String>() {
            @Override
            public String convert(Identifier source) {
                return source.getId().toString();
            }
        });
        converters.add(new Converter<String, CertificateId>() {
            @Override
            public CertificateId convert(String source) {
                return new CertificateId(source);
            }
        });
        converters.add(new Converter<String, DomainId>() {
            @Override
            public DomainId convert(String source) {
                return new DomainId(source);
            }
        });
        converters.add(new Converter<String, OrdersId>() {
            @Override
            public OrdersId convert(String source) {
                return new OrdersId(source);
            }
        });

        return new JdbcCustomConversions(converters);
    }

    @Bean
    public <R extends Identifier, T extends BaseEntity<R>> AfterSaveCallback<T> afterSaveCallback() {
        return aggregate -> {
            aggregate.setNew(false);
            return aggregate;
        };
    }

}
