package com.asrevo.cvhome.dcm.config.converters;

import com.asrevo.cvhome.dcm.domain.challenges.Challenges;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

import java.io.IOException;

@Slf4j
@ReadingConverter
public class JsonToChallengesConverter implements Converter<String, Challenges> {

    private final ObjectMapper objectMapper;

    public JsonToChallengesConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Challenges convert(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<>() {
            });
        } catch (IOException e) {
            log.error("Problem while parsing JSON: {}", json, e);
        }
        return null;
    }

}