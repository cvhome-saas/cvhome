package com.asrevo.cvhome.s2s.model;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties("com.asrevo.cvhome.app")
public class AppProperties {

    private String domain;
    private Set<String> sub=new LinkedHashSet<>();
    private List<Handler> handlers=new ArrayList<>();

    public record Handler(String schema, String port) {
    }

    public Set<String> getUrls() {
        Set<String> urls = new LinkedHashSet<>();

        Set<String> hosts = new LinkedHashSet<>();
        hosts.add(domain);
        for (String s : sub) {
            hosts.add(s + "." + domain);
        }

        for (String host : hosts) {
            for (Handler handler : handlers) {
                String url = buildUrl(handler.schema(), host, handler.port());
                urls.add(url);
            }
        }

        return urls;
    }

    private String buildUrl(String schema, String host, String port) {
        boolean isDefaultPort = switch (schema) {
            case "http" -> "80".equals(port);
            case "https" -> "443".equals(port);
            default -> false;
        };

        if (isDefaultPort) {
            return "%s://%s".formatted(schema, host);
        }
        return "%s://%s:%s".formatted(schema, host, port);
    }
}
