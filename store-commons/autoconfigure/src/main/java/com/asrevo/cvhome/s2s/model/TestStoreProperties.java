package com.asrevo.cvhome.s2s.model;

import java.util.List;
import java.util.UUID;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.test-store")
public record TestStoreProperties(List<TestUser> users) {

    public record TestUser(UUID id, String password) {
    }
}
