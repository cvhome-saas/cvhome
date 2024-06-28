package com.asrevo.session.dynamodb.config;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.session.dynamodb")
public class DynamoDBSpringSessionConfiguration {

    private Integer maxInactiveIntervalInSeconds;

    private String tableName;

}