package com.asrevo.cvhome.s2s.config.internal;

import com.asrevo.cvhome.commons.event.Event;
import com.asrevo.cvhome.s2s.utils.ObjectIdDeserializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

	@Getter
	// @formatter:off
    private static final JsonMapper ployJson =
            JsonMapper.builder()
                    .visibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
                    .serializationInclusion(JsonInclude.Include.ALWAYS)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                    .configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false)
                    .addModules(new JavaTimeModule(), new Jdk8Module())
                    .defaultDateFormat(new StdDateFormat())
                    .activateDefaultTyping(
                            BasicPolymorphicTypeValidator.builder()
                                    .allowIfBaseType(Event.class)
                                    .allowIfBaseType(Object.class)
                                    .build(),
                            ObjectMapper.DefaultTyping.NON_FINAL,
                            JsonTypeInfo.As.WRAPPER_OBJECT)
                    .build();

    // @formatter:on
	@Bean
	public Jackson2ObjectMapperBuilderCustomizer customizer() {

		return jacksonObjectMapperBuilder -> jacksonObjectMapperBuilder
			.deserializers(new ObjectIdDeserializer(ObjectId.class));
	}

}
