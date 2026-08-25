package com.asrevo.cvhome.merchant.service;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.merchant.services.merchant.MerchantRoutingService;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The pod's own domain is always granted a certificate without a database round trip; every other host is a
 * store's custom domain or nothing.
 */
class AskTlsServiceTest {

    private static final String POD_DOMAIN = "spg-507f1f77.gateway.com";

    private static final Domain CUSTOM = new Domain("shop.example.com");

    private final MerchantRoutingService routing = mock(MerchantRoutingService.class);

    private final PodInfoProperties podInfo = new PodInfoProperties(new Pod(null, "pod-507f1f77", null, null,
            POD_DOMAIN));

    private final AskTlsService askTls = new AskTlsService(routing, podInfo);

    private final LookupDomainHeadersService lookup = new LookupDomainHeadersService(routing, podInfo);

    @Test
    void thePodDomainIsGrantedWithoutLookingAtStores() {
        assertThat(askTls.ask(new Domain(POD_DOMAIN))).isTrue();

        verifyNoInteractions(routing);
    }

    @Test
    void otherDomainsAreGrantedOnlyWhenAStoreOwnsThem() {
        when(routing.containsDomain(CUSTOM, POD_DOMAIN)).thenReturn(true);

        assertThat(askTls.ask(CUSTOM)).isTrue();
        assertThat(askTls.ask(new Domain("nobody.example.com"))).isFalse();
    }

    @Test
    void headerLookupIsScopedToThePodDomain() {
        Map<String, String> headers = Map.of("Theme", "BASIS");
        when(routing.lookupHeaders(CUSTOM, POD_DOMAIN)).thenReturn(headers);

        assertThat(lookup.lookupHeaders(CUSTOM)).isEqualTo(headers);
    }

}
