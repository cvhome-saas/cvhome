package com.asrevo.cvhome.content.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;

/**
 * Content platform limits. The media quota is a plan entitlement in the requirements; until billing exposes it, it
 * is one platform-wide figure.
 */
@ConfigurationProperties("com.asrevo.cvhome.content")
public record ContentProperties(@DefaultValue Media media) {

    public record Media(@DefaultValue("5GB") DataSize quota, @DefaultValue("50MB") DataSize maxFileSize) {
    }

}
