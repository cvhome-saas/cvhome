package com.asrevo.cvhome.certificatemanager.config;

import com.asrevo.cvhome.certificatemanager.config.converters.JsonToMapConverter;
import com.asrevo.cvhome.certificatemanager.config.converters.MapToJsonConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.jdbc.core.convert.JdbcCustomConversions;
import org.springframework.data.jdbc.repository.config.AbstractJdbcConfiguration;

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
        return new JdbcCustomConversions(converters);
    }
}
