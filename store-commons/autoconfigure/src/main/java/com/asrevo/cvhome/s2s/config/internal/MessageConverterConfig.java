/*
 * package com.asrevo.cvhome.s2s.config.internal;
 *
 * import com.fasterxml.jackson.annotation.JsonTypeInfo; import
 * com.fasterxml.jackson.databind.json.JsonMapper; import
 * com.fasterxml.jackson.databind.jsontype.impl.StdTypeResolverBuilder; import
 * com.fasterxml.jackson.datatype.jdk8.Jdk8Module; import
 * com.fasterxml.jackson.datatype.jsr310.JavaTimeModule; import
 * org.springframework.cloud.function.context.config.JsonMessageConverter; import
 * org.springframework.cloud.function.json.JacksonMapper; import
 * org.springframework.context.annotation.Bean; import
 * org.springframework.context.annotation.Configuration;
 *
 * @Configuration public class MessageConverterConfig {
 *
 * @Bean public JsonMessageConverter jsonMessageConverter() { JsonMapper mapper =
 * JsonMapper.builder() .addModules(new JavaTimeModule(), new Jdk8Module())
 * .setDefaultTyping(new StdTypeResolverBuilder().init(JsonTypeInfo.Id.CLASS, null)
 * .inclusion(JsonTypeInfo.As.WRAPPER_OBJECT)) .build();
 *
 * return new JsonMessageConverter(new JacksonMapper(mapper)); }
 *
 * }
 */
