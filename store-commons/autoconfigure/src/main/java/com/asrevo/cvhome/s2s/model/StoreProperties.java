package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.store.props")
public record StoreProperties(boolean enableOrderMailApi) {}
