package com.asrevo.session.dynamodb.config;



import com.asrevo.session.dynamodb.config.annotation.EnableDynamoDBHttpSession;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportAware;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.session.config.annotation.web.http.SpringHttpSessionConfiguration;

// TODO: Clean this code from Spring Boot dependencay and Apply Auto-Configuration techniques in separate module
@Configuration
public class DynamoDBSessionRepositoryConfiguration extends SpringHttpSessionConfiguration implements ImportAware {

    public static final int MAX_INACTIVE_INTERVAL_IN_SECONDS = 20 * 60;

    public static final String SESSIONS_TABLE_NAME = "Sessions";

    private int maxInactiveIntervalInSeconds;

    private String sessionsTableName;

/*
    @Bean
    public DynamoDBSessionRepository createDynamoDBSessionRepository(DynamoDbClient dynamoDB,
                                                                     DynamoDBSpringSessionConfiguration configuration) {

        final int theMaxInactiveIntervalInSeconds = configuration.getMaxInactiveIntervalInSeconds() != null ?
                configuration
                        .getMaxInactiveIntervalInSeconds() : this.maxInactiveIntervalInSeconds;

        final String theSessionsTableName = StringUtils.hasText(configuration.getTableName()) ? configuration
                .getTableName() : this.sessionsTableName;

        return new DynamoDBSessionRepository(dynamoDB, theMaxInactiveIntervalInSeconds, theSessionsTableName);
    }
*/

    @Override
    public void setImportMetadata(AnnotationMetadata importMetadata) {
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(importMetadata.getAnnotationAttributes
                (EnableDynamoDBHttpSession.class.getName()));

        if (attributes != null) {
            this.maxInactiveIntervalInSeconds = attributes.getNumber("maxInactiveIntervalInSeconds");
            this.sessionsTableName = attributes.getString("sesstionsTableName");
        }
    }

    public void setMaxInactiveIntervalInSeconds(int maxInactiveIntervalInSeconds) {
        this.maxInactiveIntervalInSeconds = maxInactiveIntervalInSeconds;
    }

    public void setSessionsTableName(String sessionsTableName) {
        this.sessionsTableName = sessionsTableName;
    }

}