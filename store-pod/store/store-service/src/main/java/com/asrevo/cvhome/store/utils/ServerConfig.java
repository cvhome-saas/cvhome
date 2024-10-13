package com.asrevo.cvhome.store.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Setter
@Getter
@Component
public class ServerConfig implements ApplicationListener<WebServerInitializedEvent> {

    private String applicationHost = null;

    @Override
    public void onApplicationEvent(final WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();
        final String host = getHost();
        setApplicationHost(String.join(":", host, String.valueOf(port)));
    }

    private String getHost() {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return "127.0.0.1";
        }
    }
}
