package com.asrevo.cvhome.s2s.model;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.cvhome.app")
public class AppProperties {

    private String domain;

}
