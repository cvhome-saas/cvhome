package com.asrevo.cvhome.product.config.converters;

import com.asrevo.cvhome.product.commons.domain.ImagesLink;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@ReadingConverter
public class JsonToImagesLinkConverter implements Converter<String, ImagesLink> {

    private final ObjectMapper objectMapper;

    public JsonToImagesLinkConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public ImagesLink convert(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("Problem while parsing JSON: {}", json, e);
        }
        return null;
    }

}