package com.asrevo.session.dynamodb;

import org.springframework.session.Session;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

public class DynamodbSession implements Session {
    @Override
    public String getId() {
        return "";
    }

    @Override
    public String changeSessionId() {
        return "";
    }

    @Override
    public <T> T getAttribute(String attributeName) {
        return null;
    }

    @Override
    public Set<String> getAttributeNames() {
        return Set.of();
    }

    @Override
    public void setAttribute(String attributeName, Object attributeValue) {

    }

    @Override
    public void removeAttribute(String attributeName) {

    }

    @Override
    public Instant getCreationTime() {
        return null;
    }

    @Override
    public void setLastAccessedTime(Instant lastAccessedTime) {

    }

    @Override
    public Instant getLastAccessedTime() {
        return null;
    }

    @Override
    public void setMaxInactiveInterval(Duration interval) {

    }

    @Override
    public Duration getMaxInactiveInterval() {
        return null;
    }

    @Override
    public boolean isExpired() {
        return false;
    }
}
