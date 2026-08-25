package com.asrevo.cvhome.merchant.services.merchant;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.ColorTheme;
import com.asrevo.cvhome.commons.domain.Domain;
import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.Theme;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.repositories.merchant.MerchantRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Domain routing: which headers the gateway receives for a host, and how custom domains are attached and detached.
 */
class MerchantRoutingServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final String NAME = "Shop";

    private static final StoreMerchantId UNKNOWN = new StoreMerchantId("65f023632bc46470c104b00f");

    private static final String POD_DOMAIN = "spg.gateway.com";

    private static final Domain CUSTOM = new Domain("shop.example.com");

    private static final ManagerStoreDomain SUB = new ManagerStoreDomain("shop", DomainType.SUB_DOMAIN);

    private static final LanguageCode AR = new LanguageCode("ar");

    private static final String STORE_ID_HEADER = "Store-Id";

    private static final String LANGUAGES_HEADER = "Supported-Languages";

    private MerchantRepository repository;

    private MerchantRoutingService service;

    @BeforeEach
    void setUp() {
        repository = mock(MerchantRepository.class);
        service = new MerchantRoutingService(repository);
    }

    private MerchantStore stored() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        store.getStoreDomains().add(SUB);
        when(repository.findById(STORE)).thenReturn(Optional.of(store));
        return store;
    }

    @Test
    void containsDomainReflectsTheRepository() {
        when(repository.findByDomain(CUSTOM.domain(), POD_DOMAIN)).thenReturn(Optional.of(new MerchantStore()));

        assertThat(service.containsDomain(CUSTOM, POD_DOMAIN)).isTrue();
        assertThat(service.containsDomain(new Domain("nobody.example.com"), POD_DOMAIN)).isFalse();
    }

    @Test
    void lookupHeadersDescribeTheStoreBehindTheDomain() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        store.setTheme(Theme.BASIS);
        store.setColorTheme(ColorTheme.DARK);
        store.setDefaultLanguageCode(AR);
        store.setLanguages(List.of(AR, new LanguageCode("en")));
        when(repository.findByDomain(CUSTOM.domain(), POD_DOMAIN)).thenReturn(Optional.of(store));

        Map<String, String> headers = service.lookupHeaders(CUSTOM, POD_DOMAIN);

        assertThat(headers).containsEntry(STORE_ID_HEADER, STORE.getId())
                .containsEntry("Theme", "BASIS")
                .containsEntry("Color-Theme", "DARK")
                .containsEntry("Default-Language", AR.code())
                .containsEntry(LANGUAGES_HEADER, "ar,en");
    }

    @Test
    void lookupHeadersSkipWhatTheStoreDoesNotHave() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        when(repository.findByDomain(CUSTOM.domain(), POD_DOMAIN)).thenReturn(Optional.of(store));

        Map<String, String> headers = service.lookupHeaders(CUSTOM, POD_DOMAIN);

        assertThat(headers).containsOnlyKeys(STORE_ID_HEADER, LANGUAGES_HEADER).containsEntry(LANGUAGES_HEADER, "");
    }

    @Test
    void lookupHeadersOfAnUnknownDomainAreEmpty() {
        assertThat(service.lookupHeaders(CUSTOM, POD_DOMAIN)).isEmpty();
    }

    @Test
    void domainsListsWhatIsAllocated() throws MerchantStoreNotFoundException {
        stored();

        assertThat(service.domains(STORE)).containsExactly(SUB);
    }

    @Test
    void addDomainAttachesACustomDomainAndSaves() throws MerchantStoreNotFoundException {
        MerchantStore store = stored();

        service.addDomain(STORE, CUSTOM);

        assertThat(store.getStoreDomains()).contains(SUB, new ManagerStoreDomain(CUSTOM.domain(),
                DomainType.CUSTOM_DOMAIN));
        verify(repository).save(store);
    }

    @Test
    void removeDomainDetachesItAndSaves() throws MerchantStoreNotFoundException {
        MerchantStore store = stored();
        store.getStoreDomains().add(new ManagerStoreDomain(CUSTOM.domain(), DomainType.CUSTOM_DOMAIN));

        service.removeDomain(STORE, CUSTOM);

        assertThat(store.getStoreDomains()).containsExactly(SUB);
        verify(repository).save(store);
    }

    @Test
    void unknownStoreIsRefusedEverywhere() {
        assertThatThrownBy(() -> service.domains(UNKNOWN)).isInstanceOf(MerchantStoreNotFoundException.class);
        assertThatThrownBy(() -> service.addDomain(UNKNOWN, CUSTOM)).isInstanceOf(MerchantStoreNotFoundException.class);
        assertThatThrownBy(() -> service.removeDomain(UNKNOWN, CUSTOM))
                .isInstanceOf(MerchantStoreNotFoundException.class);
        verify(repository, never()).save(any());
    }

}
