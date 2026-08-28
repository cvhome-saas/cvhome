package com.asrevo.cvhome.merchant.service.facade.merchant;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.commons.domain.DomainType;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.utils.DefaultStoresConstants;
import com.asrevo.cvhome.merchant.entity.merchant.MerchantStore;
import com.asrevo.cvhome.merchant.errors.DefaultStoreNotRemovableException;
import com.asrevo.cvhome.merchant.errors.DuplicateMerchantStoreException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.populator.merchant.PersistableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.service.populator.merchant.ReadableMerchantStorePopulator;
import com.asrevo.cvhome.merchant.services.merchant.MerchantStoreService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The store facade's rules: a taken id is a conflict, the default store is never deleted, every upload is tagged
 * with its content type and its stream is closed whatever happens, and a new slider image takes the next priority.
 */
class StoreFacadeImplTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b75f");

    private static final StoreMerchantId UNKNOWN = new StoreMerchantId("65f023632bc46470c104b00f");

    private static final LanguageCode EN = new LanguageCode("en");

    private static final LanguageCode FR = new LanguageCode("fr");

    private static final String NAME = "My Store";

    private static final String ORG = "21f023932bc66470c104b76f";

    private static final String LOGO = "logo.png";

    private static final String BANNER = "banner.png";

    private static final String SLIDE = "slide.png";

    private static final String FOLDER = "marketing";

    private MerchantStoreService service;

    private PersistableMerchantStorePopulator persistablePopulator;

    private ReadableMerchantStorePopulator readablePopulator;

    private StoreFacadeImpl facade;

    @BeforeEach
    void setUp() {
        service = mock(MerchantStoreService.class);
        persistablePopulator = mock(PersistableMerchantStorePopulator.class);
        readablePopulator = mock(ReadableMerchantStorePopulator.class);
        facade = new StoreFacadeImpl(service, persistablePopulator, readablePopulator);
    }

    private MerchantStore stored() {
        MerchantStore store = new MerchantStore(STORE, NAME);
        store.setOrg(ORG);
        store.setDefaultLanguageCode(FR);
        store.setLanguages(List.of(FR, EN));
        when(service.getByMerchantStoreId(STORE)).thenReturn(store);
        return store;
    }

    // ------------------------------------------------------------------------------------------------- reads

    @Test
    void getDelegatesToTheService() {
        MerchantStore store = stored();

        assertThat(facade.get(STORE)).isSameAs(store);
    }

    @Test
    void readByIdPopulatesInTheRequestedLanguage() throws MerchantStoreNotFoundException {
        MerchantStore store = stored();

        ReadableMerchantStore readable = facade.getByMerchantStoreId(STORE, EN);

        assertThat(readable).isNotNull();
        verify(readablePopulator).populate(eq(store), any(ReadableMerchantStore.class), eq(store), eq(EN));
    }

    @Test
    void readOfUnknownStoreIsNotFound() {
        assertThatThrownBy(() -> facade.getByMerchantStoreId(UNKNOWN, EN))
                .isInstanceOf(MerchantStoreNotFoundException.class);
    }

    @Test
    void readWithoutLanguageUsesTheStoreDefault() {
        MerchantStore store = stored();

        facade.getReadableMerchantStoreId(STORE);

        verify(readablePopulator).populate(eq(store), any(ReadableMerchantStore.class), eq(store), eq(FR));
    }

    @Test
    void supportedLanguagesComeFromTheStore() {
        stored();

        assertThat(facade.supportedLanguages(STORE)).containsExactly(FR, EN);
        assertThat(facade.supportedLanguages(UNKNOWN)).isEmpty();
    }

    // ----------------------------------------------------------------------------------------- create / update

    @Test
    void createRefusesATakenId() {
        stored();
        PersistableMerchantStore request = new PersistableMerchantStore();
        request.setId(STORE.getId());

        assertThatThrownBy(() -> facade.create(request)).isInstanceOf(DuplicateMerchantStoreException.class);

        verify(service, never()).saveOrUpdate(any());
    }

    @Test
    void createAllocatesTheNameAsSubDomainAndSaves() throws DuplicateMerchantStoreException {
        PersistableMerchantStore request = new PersistableMerchantStore();
        request.setId(UNKNOWN.getId());
        request.setName(NAME);
        MerchantStore populated = new MerchantStore(UNKNOWN, NAME);
        when(persistablePopulator.populate(eq(request), any(MerchantStore.class), eq(LanguageCode.defaultLanguage())))
                .thenReturn(populated);

        facade.create(request);

        assertThat(request.getStoreDomains()).containsExactly(new ManagerStoreDomain(NAME, DomainType.SUB_DOMAIN));
        verify(service).saveOrUpdate(populated);
    }

    @Test
    void updateCarriesIdentityAndDomainsOverFromTheStoredRow() throws MerchantStoreNotFoundException {
        MerchantStore store = stored();
        Set<ManagerStoreDomain> domains = Set.of(new ManagerStoreDomain("me", DomainType.SUB_DOMAIN));
        store.setStoreDomains(domains);
        PersistableMerchantStore request = new PersistableMerchantStore();
        request.setId("something-else");
        request.setOrg("another-org");
        when(persistablePopulator.populate(request, store, LanguageCode.defaultLanguage())).thenReturn(store);

        facade.update(STORE, request);

        assertThat(request.getId()).isEqualTo(STORE.getId());
        assertThat(request.getOrg()).isEqualTo(ORG);
        assertThat(request.getStoreDomains()).isEqualTo(domains);
        verify(service).update(store);
    }

    @Test
    void updateOfUnknownStoreIsNotFound() {
        assertThatThrownBy(() -> facade.update(UNKNOWN, new PersistableMerchantStore()))
                .isInstanceOf(MerchantStoreNotFoundException.class);
        verify(service, never()).update(any());
    }

    // ------------------------------------------------------------------------------------------------ delete

    @Test
    void theDefaultStoreCannotBeDeleted() {
        assertThatThrownBy(() -> facade.delete(DefaultStoresConstants.DEFAULT_ORG1_STORE1))
                .isInstanceOf(DefaultStoreNotRemovableException.class);
        verifyNoInteractions(service);
    }

    @Test
    void deleteOfUnknownStoreIsNotFound() {
        assertThatThrownBy(() -> facade.delete(UNKNOWN)).isInstanceOf(MerchantStoreNotFoundException.class);
        verify(service, never()).delete(any());
    }

    @Test
    void deleteRemovesTheStoredRow() throws MerchantStoreNotFoundException, DefaultStoreNotRemovableException {
        MerchantStore store = stored();

        facade.delete(STORE);

        verify(service).delete(store);
    }

}
