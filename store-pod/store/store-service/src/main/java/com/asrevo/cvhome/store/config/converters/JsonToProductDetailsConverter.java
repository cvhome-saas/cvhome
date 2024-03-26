package com.asrevo.cvhome.store.config.converters;

import com.asrevo.cvhome.storepod.commons.domain.ProductDetails;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;

@Slf4j
@ReadingConverter
public class JsonToProductDetailsConverter implements Converter<String, ProductDetails> {

    private final ObjectMapper objectMapper;

    public JsonToProductDetailsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ProductDetails convert(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("Problem while parsing JSON: {}", json, e);
        }
        return null;
    }

}