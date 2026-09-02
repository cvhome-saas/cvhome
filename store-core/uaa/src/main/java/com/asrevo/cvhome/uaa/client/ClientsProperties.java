package com.asrevo.cvhome.uaa.client;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Registration policy.
 *
 * @param plainHttpHosts the hosts a redirect URI may use over plain {@code http}; an entry starting with a dot matches
 *                       every subdomain. Everything else must be {@code https}
 */
@ConfigurationProperties(prefix = "com.asrevo.cvhome.uaa.clients")
public record ClientsProperties(@DefaultValue({"localhost", "127.0.0.1"}) List<String> plainHttpHosts) {
}
