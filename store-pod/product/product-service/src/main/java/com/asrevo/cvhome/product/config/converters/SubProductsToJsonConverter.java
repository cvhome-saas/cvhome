package com.asrevo.cvhome.product.config.converters;

import com.asrevo.cvhome.product.commons.domain.SubProducts;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
@Slf4j
public class SubProductsToJsonConverter implements Converter<SubProducts, String> {
    private final ObjectMapper objectMapper;

    public SubProductsToJsonConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public String convert(SubProducts source) {
        try {
            return objectMapper.writeValueAsString(source);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while serializing map to JSON: {}", source, e);
        }
        return "{}";

    }
}