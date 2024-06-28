package com.asrevo.session.dynamodb.config.annotation;



import com.asrevo.session.dynamodb.config.DynamoDBSessionRepositoryConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(DynamoDBSessionRepositoryConfiguration.class)
public @interface EnableDynamoDBHttpSession {

    int maxInactiveIntervalInSeconds() default DynamoDBSessionRepositoryConfiguration.MAX_INACTIVE_INTERVAL_IN_SECONDS;

    String sesstionsTableName() default DynamoDBSessionRepositoryConfiguration.SESSIONS_TABLE_NAME;

}