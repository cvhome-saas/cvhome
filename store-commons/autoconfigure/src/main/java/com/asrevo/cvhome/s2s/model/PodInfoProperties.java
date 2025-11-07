package com.asrevo.cvhome.s2s.model;

import com.asrevo.cvhome.commons.domain.Pod;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("com.asrevo.cvhome.pod-info")
public record PodInfoProperties(Pod pod) {
}
