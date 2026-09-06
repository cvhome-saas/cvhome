package com.asrevo.cvhome.s2s.utils;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import com.asrevo.cvhome.commons.domain.ServiceDomain;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The absolute URL a redirect is built from: the forwarded scheme and port when a proxy set them, the request's own
 * otherwise; the path joined with exactly one slash.
 */
class RedirectionUrlBuilderTest {

    private static final String HTTPS = "https";

    private static final String HTTP = "http";

    private static final String BASE = "https://gateway.com:8443";

    private static final String LOGIN = "/login";

    private static final String REQUEST_URI = "http://localhost:8000/x";

    private final RedirectionUrlBuilder builder = new RedirectionUrlBuilder(HTTPS, 8443,
            new ServiceDomain("store-core-gateway", "gateway.com", "8000", HTTP, "store-core.cvhome.lcl", null));

    @Test
    void thePathIsJoinedWithOneSlashAndTheRootAddsNone() {
        assertThat(builder.getRedirectionUrl("")).isEqualTo(BASE);
        assertThat(builder.getRedirectionUrl("/")).isEqualTo(BASE);
        assertThat(builder.getRedirectionUrl(LOGIN)).isEqualTo(BASE.concat(LOGIN));
        assertThat(builder.getRedirectionUrl("login")).isEqualTo(BASE.concat(LOGIN));
    }

    @Test
    void forwardedHeadersWinOverTheRequestUri() {
        var forwarded = MockServerHttpRequest.get(REQUEST_URI)
                .header(RedirectionUrlBuilder.SCHEMA_HEADER_KEY, " https ")
                .header(RedirectionUrlBuilder.PORT_HEADER_KEY, "443").build();
        var plain = MockServerHttpRequest.get(REQUEST_URI).build();

        assertThat(RedirectionUrlBuilder.getScheme(forwarded)).isEqualTo(HTTPS);
        assertThat(RedirectionUrlBuilder.getPort(forwarded)).isEqualTo(443);
        assertThat(RedirectionUrlBuilder.getScheme(plain)).isEqualTo(HTTP);
        assertThat(RedirectionUrlBuilder.getPort(plain)).isEqualTo(8000);
        assertThat(RedirectionUrlBuilder.getHeaderValue(null, RedirectionUrlBuilder.PORT_HEADER_KEY)).isEmpty();
        assertThat(RedirectionUrlBuilder.getHeaderValue(plain, null)).isEmpty();
    }

}
