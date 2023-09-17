package com.asrevo.cvhome.domain.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.DefaultBaseTypeLimitingValidator;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

public class JacksonConfig {
    @Getter
    // @formatter:off
    private static final JsonMapper ployJson = JsonMapper.builder()
            .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
            .serializationInclusion(JsonInclude.Include.ALWAYS)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
            .addModules(new JavaTimeModule(), new Jdk8Module()).defaultDateFormat(new StdDateFormat())
            .activateDefaultTyping(new DefaultBaseTypeLimitingValidator(),
                    ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_OBJECT).build();
    // @formatter:on

}
