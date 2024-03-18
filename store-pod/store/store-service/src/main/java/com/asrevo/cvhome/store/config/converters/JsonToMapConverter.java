package com.asrevo.cvhome.store.config.converters;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@ReadingConverter
public class JsonToMapConverter implements Converter<String, Map<String, String>> {

    private final ObjectMapper objectMapper;

    public JsonToMapConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Map<String, String> convert(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("Problem while parsing JSON: {}", json, e);
        }
        return null;
    }

}