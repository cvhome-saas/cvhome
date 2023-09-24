package com.asrevo.cvhome.domainownership.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.support.MessageBuilder;

@Configuration
public class MessagingConfig {
    @Bean
    public MessageConverter polyJsonMessageConverter() {
        ObjectMapper ployJson = JacksonConfig.getPloyJson();

        return new MessageConverter() {
            @Override
            public Object fromMessage(Message<?> message, Class<?> targetClass) {
                try {
                    return ployJson.readValue(new String(((byte[]) message.getPayload())), targetClass);
                } catch (JsonProcessingException e) {
                    return null;
                }
            }

            @Override
            public Message<?> toMessage(Object payload, MessageHeaders headers) {
                try {
                    return MessageBuilder.createMessage(ployJson.writeValueAsBytes(payload), headers);
                } catch (JsonProcessingException e) {
                    return null;
                }
            }
        };
    }
}
