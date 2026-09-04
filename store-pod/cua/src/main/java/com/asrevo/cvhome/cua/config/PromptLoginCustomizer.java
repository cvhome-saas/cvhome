package com.asrevo.cvhome.cua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import com.asrevo.cvhome.cua.security.PromptLoginFilter;
import com.asrevo.cvhome.sso.config.AuthorizationServerHttpCustomizer;

/**
 * Honours {@code prompt=login} on the authorization-server chain.
 *
 * <p>
 * A storefront that wants the shopper to prove who they are again sends {@code prompt=login}; the filter ends the
 * signed-in session once the context is loaded, so the chain below treats the request as anonymous and hands the
 * shopper to the form. It has to run after {@link SecurityContextHolderFilter} for the session to exist yet, and
 * before the chain decides whether anyone is signed in — which is why it is a filter rather than anything the
 * shared chain could express.
 * </p>
 */
@Configuration
public class PromptLoginCustomizer {

    @Bean
    AuthorizationServerHttpCustomizer promptLogin() {
        return http -> http.addFilterAfter(new PromptLoginFilter(), SecurityContextHolderFilter.class);
    }

}
