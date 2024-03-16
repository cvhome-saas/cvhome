package com.asrevo.cvhome.product.config.converters;

import com.asrevo.cvhome.product.commons.domain.ProductDetails;
import com.asrevo.cvhome.product.commons.domain.SubProducts;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;

@Slf4j
@ReadingConverter
public class JsonToSubProductsConverter implements Converter<String, SubProducts> {

    private final ObjectMapper objectMapper;

    public JsonToSubProductsConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public SubProducts convert(String json) {
        try {
            return objectMapper.readValue(json, SubProducts.class);
        } catch (IOException e) {
            log.error("Problem while parsing JSON: {}", json, e);
        }
        return null;
    }

}