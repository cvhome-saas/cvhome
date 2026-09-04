package com.asrevo.cvhome.s2s.config.internal;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.MethodParameter;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.LocaleResolver;

import com.asrevo.cvhome.commons.annotation.OrgStorePrincipalInfo;
import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.ServiceDomain;
import com.asrevo.cvhome.s2s.model.ServiceDomainProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The request-scoped plumbing every servlet service inherits.
 *
 * <p>
 * The locale interceptor is the one with a real reason to exist. A shopper who picks a language and then signs in
 * comes back through a saved request, and by then the {@code lang} parameter is on the <em>saved</em> request, not
 * the current one — so reading only {@code request.getParameter} loses the choice at exactly the moment a user
 * notices. It looks in both, in that order.
 * </p>
 *
 * <p>
 * {@link ServiceUrlBuilder} decides between an {@code lb://} name and a gateway-qualified address by comparing
 * namespaces. Getting it backwards produces a URL that resolves in one environment and not the other, which is
 * the kind of thing that only fails after a deploy.
 * </p>
 */
class WebPlumbingTest {

    private static final String LANG = "lang";
    private static final String ARABIC = "ar";
    private static final String CATALOG = "catalog";
    private static final String OWN_NAMESPACE = "pod-1";
    private static final String ANNOTATED = "annotatedSample";
    private static final String ORG_ADMIN = "ROLE_ORG_ADMIN";
    private static final String PORT = "8080";
    private static final String SCHEME = "http";
    private static final String SPG = "spg";
    private static final String SELF = "self";
    private static final String APP_NAME_KEY = "spring.application.name";
    private static final String POD_NAME = "p";
    private static final String POD_URL = "https://pod-1.example.com";

    private final LocaleResolver localeResolver = Mockito.mock(LocaleResolver.class);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    /** ServletWebConfig wires it with paramName "lang"; the default is "locale", so the test wires it the same way. */
    private static RequestCacheAwareLocaleInterceptor interceptor() {
        RequestCacheAwareLocaleInterceptor created = new RequestCacheAwareLocaleInterceptor();
        created.setParamName(LANG);
        return created;
    }

    private MockHttpServletRequest requestWithLocaleResolver() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE, localeResolver);
        return request;
    }

    @Test
    void alanguageOnTheCurrentRequestIsHonoured() throws Exception {
        RequestCacheAwareLocaleInterceptor interceptor = interceptor();
        MockHttpServletRequest request = requestWithLocaleResolver();
        request.setParameter(LANG, ARABIC);
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(interceptor.preHandle(request, response, null)).isTrue();

        verify(localeResolver).setLocale(request, response, Locale.forLanguageTag(ARABIC));
    }

    @Test
    void aRequestWithNoLanguageAnywhereChangesNothingAndStillProceeds() throws Exception {
        RequestCacheAwareLocaleInterceptor interceptor = interceptor();

        assertThat(interceptor.preHandle(requestWithLocaleResolver(), new MockHttpServletResponse(), null)).isTrue();

        verify(localeResolver, Mockito.never()).setLocale(any(), any(), any());
    }

    @Test
    void anInvalidLanguageIsIgnoredByDefaultRatherThanFailingTheRequest() throws Exception {
        RequestCacheAwareLocaleInterceptor interceptor = interceptor();
        interceptor.setIgnoreInvalidLocale(true);
        MockHttpServletRequest request = requestWithLocaleResolver();
        request.setParameter(LANG, "not a locale at all");

        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), null)).isTrue();
    }

    @Test
    void aServiceWithNoLocaleResolverIsLeftAlone() throws Exception {
        RequestCacheAwareLocaleInterceptor interceptor = interceptor();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter(LANG, ARABIC);

        // A reactive or headless service has none; the interceptor must not assume one.
        assertThat(interceptor.preHandle(request, new MockHttpServletResponse(), null)).isTrue();
    }

    @Test
    void theIdentityResolverAnswersOnlyForTheAnnotatedParameter() throws Exception {
        ServletOrgStorePrincipalInfoArgumentResolver resolver =
                new ServletOrgStorePrincipalInfoArgumentResolver();

        MethodParameter annotated = new MethodParameter(
                WebPlumbingTest.class.getDeclaredMethod(ANNOTATED, Object.class), 0);
        MethodParameter plain = new MethodParameter(
                WebPlumbingTest.class.getDeclaredMethod("plainSample", Object.class), 0);

        assertThat(resolver.supportsParameter(annotated)).isTrue();
        assertThat(resolver.supportsParameter(plain)).isFalse();
    }

    @Test
    void theIdentityResolverReadsWhoeverIsInTheSecurityContext() throws Exception {
        ServletOrgStorePrincipalInfoArgumentResolver resolver =
                new ServletOrgStorePrincipalInfoArgumentResolver();
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "none").subject("s")
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(60))
                .claim("org", "21f023932bc66470c104b76f")
                .claim("store", "65f023632bc46470c104b76f")
                .claim("roles", List.of(ORG_ADMIN)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority(ORG_ADMIN))));

        MethodParameter annotated = new MethodParameter(
                WebPlumbingTest.class.getDeclaredMethod(ANNOTATED, Object.class), 0);

        assertThat(resolver.resolveArgument(annotated, null, null, null)).isNotNull();
    }

    private static ServiceDomainProperties domains(String requestedNamespace) {
        Map<String, ServiceDomain> services = Map.of(
                CATALOG, new ServiceDomain(CATALOG, "catalog.example.com", PORT, SCHEME, requestedNamespace, SPG),
                SELF, new ServiceDomain(SELF, "self.example.com", PORT, SCHEME, OWN_NAMESPACE, SPG),
                SPG, new ServiceDomain(SPG, "spg.example.com", PORT, SCHEME, requestedNamespace, SPG));
        return new ServiceDomainProperties(services, List.of());
    }

    @Test
    void aServiceInTheSameNamespaceIsAddressedByItsPlainLoadBalancerName() {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.getProperty(APP_NAME_KEY)).thenReturn(SELF);

        assertThat(new ServiceUrlBuilder(domains(OWN_NAMESPACE), environment).getServiceUrl(CATALOG))
                .isEqualTo("lb://catalog");
    }

    @Test
    void aServiceInAnotherNamespaceGoesThroughThatNamespacesGateway() {
        Environment environment = Mockito.mock(Environment.class);
        when(environment.getProperty(APP_NAME_KEY)).thenReturn(SELF);

        assertThat(new ServiceUrlBuilder(domains("pod-2"), environment).getServiceUrl(CATALOG))
                .isEqualTo("lb://spg.pod-2/catalog");
    }

    @Test
    void aPodIsAddressedThroughItsOwnSpgWhenInternalAndDirectlyWhenExternal() {
        ServiceUrlBuilder builder = new ServiceUrlBuilder(domains(OWN_NAMESPACE), Mockito.mock(Environment.class));
        Pod internal = new Pod(null, POD_NAME, new PodEndpoint("pod-1.internal", EndpointType.INTERNAL), null, null);
        Pod external = new Pod(null, POD_NAME, new PodEndpoint(POD_URL, EndpointType.EXTERNAL),
                null, null);
        Pod unspecified = new Pod(null, POD_NAME, new PodEndpoint(POD_URL, null), null, null);

        assertThat(builder.getServiceUrl(internal)).isEqualTo("lb://spg.pod-1.internal");
        assertThat(builder.getServiceUrl(external)).isEqualTo(POD_URL);
        assertThat(builder.getServiceUrl(unspecified)).isEqualTo(POD_URL);
        assertThat(builder.getServiceUrl(external, CATALOG)).isEqualTo("https://pod-1.example.com/catalog");
    }

    @SuppressWarnings("unused")
    private void annotatedSample(@OrgStorePrincipalInfo Object identity) {
        // Only a MethodParameter source.
    }

    @SuppressWarnings("unused")
    private void plainSample(Object identity) {
        // Only a MethodParameter source.
    }
}
