package com.asrevo.cvhome.merchant.api.v1;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.service.AskTlsService;
import com.asrevo.cvhome.merchant.service.LookupDomainHeadersService;
import com.asrevo.cvhome.merchant.services.merchant.MerchantRoutingService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The TLS answer is a status, not a body: Caddy's on-demand TLS asks and only reads the code.
 */
class RouterControllerTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final Domain DOMAIN = new Domain("shop.example.com");

    private final MerchantRoutingService routing = mock(MerchantRoutingService.class);

    private final AskTlsService askTls = mock(AskTlsService.class);

    private final LookupDomainHeadersService lookup = mock(LookupDomainHeadersService.class);

    private final RouterController controller = new RouterController(routing, askTls, lookup);

    @Test
    void tlsIsGrantedWithOkAndRefusedWithBadRequest() {
        when(askTls.ask(DOMAIN)).thenReturn(true);

        assertThat(controller.ask(DOMAIN).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(controller.ask(new Domain("nobody.example.com")).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void headerLookupIsPassedThrough() {
        Map<String, String> headers = Map.of("Store-Id", STORE.getId());
        when(lookup.lookupHeaders(DOMAIN)).thenReturn(headers);

        assertThat(controller.getLookupHeadersByDomain(DOMAIN)).isEqualTo(headers);
    }

    @Test
    void domainManagementDelegatesToTheRoutingService() throws MerchantStoreNotFoundException {
        Set<ManagerStoreDomain> domains = Set.of(new ManagerStoreDomain(DOMAIN.domain(), DomainType.CUSTOM_DOMAIN));
        when(routing.domains(STORE)).thenReturn(domains);

        assertThat(controller.allocatedDomains(STORE)).isEqualTo(domains);
        controller.allocate(STORE, DOMAIN);
        controller.remove(STORE, DOMAIN);

        verify(routing).addDomain(STORE, DOMAIN);
        verify(routing).removeDomain(STORE, DOMAIN);
    }

}
