package com.asrevo.cvhome.cua.config;

import java.util.Objects;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.asrevo.cvhome.s2s.utils.UrlNormalize;
import com.asrevo.cvhome.sso.config.IssuerPin;

/**
 * cua's issuer: one per pod, and never the request host.
 *
 * <p>
 * Pinning is not optional here and it cannot be replaced by trusting the hosts cua answers on. The browser always
 * reaches cua same-origin, so the request host is the shopper's storefront host — a per-store subdomain, or an
 * arbitrary merchant-owned custom domain. That set is unbounded and can never be enumerated in a resource
 * server's trust list. One issuer per pod is the only shape that works, and every pod service's trust list is
 * written against exactly that.
 * </p>
 *
 * <p>
 * This is also why the realm is not in the issuer. A realm per store with an issuer per store would put the
 * hundred-thousandth store's URL in every resource server's configuration; the store travels in a claim instead,
 * and the issuer stays one value the pod's services already trust.
 * </p>
 *
 * <p>
 * Failing to boot is deliberate. Without pod info Spring Authorization Server falls back to deriving the issuer
 * per request, which — with the storefront host passed through and {@code X-Forwarded-Prefix} honoured — silently
 * produced exactly the per-store issuer no resource server trusts: shoppers could sign in and then be rejected by
 * the first API call they made. A cua that cannot pin its issuer mints tokens nothing will accept.
 * </p>
 *
 * <p>
 * The endpoint is normalized on the way in because it is operator-entered free text in the pod registry: that is
 * what keeps a pod registered as {@code https://host:443} from advertising an issuer that differs, character for
 * character, from the same pod registered as {@code https://host}.
 * </p>
 */
@Configuration
public class PodEndpointIssuerPin {

    private static final String UNPINNED = """
            cua cannot pin its OAuth2 issuer: com.asrevo.cvhome.pod-info.pod.endpoint.endpoint is not configured. \
            Without it the issuer is derived from the request host, and every token this server mints is rejected \
            downstream.""";

    @Bean
    IssuerPin issuerPin(PodInfoProperties properties) {
        return () -> {
            Pod pod = properties.pod();
            if (Objects.isNull(pod) || Objects.isNull(pod.endpoint()) || Objects.isNull(pod.endpoint().endpoint())) {
                throw new IllegalStateException(UNPINNED);
            }
            return UrlNormalize.normalizeUri("%s/cua".formatted(pod.endpoint().endpoint()));
        };
    }

}
