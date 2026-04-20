package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.asrevo.cvhome.commons.domain.Pod;

@ConfigurationProperties("com.asrevo.cvhome.pod-info")
public record PodInfoProperties(Pod pod) {
}
