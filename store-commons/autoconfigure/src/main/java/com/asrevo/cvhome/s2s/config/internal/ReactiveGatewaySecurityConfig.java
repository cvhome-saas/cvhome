package com.asrevo.cvhome.s2s.config.internal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.asrevo.cvhome.s2s.config.security.SecurityContextServerLogoutHandler;
import com.asrevo.cvhome.s2s.config.security.UaaLogoutSuccessHandler;

@Configuration
public class ReactiveGatewaySecurityConfig {

    @Bean
    public UaaLogoutSuccessHandler logoutSuccessHandler(Environment environment) {
        return new UaaLogoutSuccessHandler(
                environment.getProperty("spring.security.oauth2.client.provider.uaa.end-session-endpoint"),
                /*
                 * Empty unless the gateway forwards uaa under a path of its own. Where it does, uaa's session
                 * cookie lives on this origin and only a same-origin end-session call can clear it.
                 */
                environment.getProperty("com.asrevo.cvhome.gateway.uaa-path-prefix", ""));
    }

    @Bean
    public SecurityContextServerLogoutHandler logoutHandler() {
        return new SecurityContextServerLogoutHandler();
    }

}
