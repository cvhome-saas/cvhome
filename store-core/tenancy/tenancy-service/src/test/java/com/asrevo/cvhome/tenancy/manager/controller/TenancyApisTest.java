package com.asrevo.cvhome.tenancy.manager.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.ColorTheme;
import com.asrevo.cvhome.commons.domain.EndpointType;
import com.asrevo.cvhome.commons.domain.ManagerOrgId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.PodEndpoint;
import com.asrevo.cvhome.commons.domain.PodId;
import com.asrevo.cvhome.commons.domain.Roles;
import com.asrevo.cvhome.commons.domain.SocialProvider;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.Theme;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.podregistry.commons.errors.PodNotFoundException;
import com.asrevo.cvhome.podregistry.services.pod.CachingPodDirectory;
import com.asrevo.cvhome.tenancy.commons.dto.ListManagerStoreQuery;
import com.asrevo.cvhome.tenancy.manager.service.InternalStoreService;
import com.asrevo.cvhome.tenancy.manager.service.StoreLifecycleService;
import com.asrevo.cvhome.tenancy.manager.service.StoreManagerService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The tenancy console endpoints, and the three that do more than delegate.
 *
 * <p>
 * {@link RouterApi} is the call the console makes to enter a store, so it is where a suspended store has to bite:
 * it asks {@code requireOperable} before resolving the pod, and a store bound to a pod the registry has forgotten
 * is a typed 404 rather than a null the console would render as an empty shop. {@link StoreLifecycleApi} defaults
 * both the actor and the suspension reason, because each lands in an audit row that is useless when it says null.
 * </p>
 */
class TenancyApisTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final ManagerOrgId ORG = new ManagerOrgId("21f023932bc66470c104b76f");
    private static final PodId POD = new PodId("507f1f77bcf86cd799439011");
    private static final String OPERATOR = "ops@example.com";
    private static final String UNKNOWN_ACTOR = "unknown";
    private static final String STORE_NAME = "shop";
    private static final String REASON = "non-payment";

    private final InternalStoreService internalStoreService = Mockito.mock(InternalStoreService.class);
    private final StoreManagerService managerService = Mockito.mock(StoreManagerService.class);
    private final StoreLifecycleService lifecycleService = Mockito.mock(StoreLifecycleService.class);
    private final CachingPodDirectory podDirectory = Mockito.mock(CachingPodDirectory.class);

    private final StoreManagerApi storeManagerApi = new StoreManagerApi(managerService, internalStoreService);
    private final RouterApi routerApi = new RouterApi(internalStoreService, podDirectory);
    private final StoreLifecycleApi lifecycleApi = new StoreLifecycleApi(lifecycleService);

    private static UserOrgStoreIdentity identity() {
        return new UserOrgStoreIdentity(ORG, STORE, Set.of(Roles.ROLE_ORG_ADMIN));
    }

    private static Authentication operator() {
        return new UsernamePasswordAuthenticationToken(OPERATOR, null, List.of());
    }

    private static Pod pod() {
        return new Pod(POD, "pod-a", new PodEndpoint("http://pod.example", EndpointType.EXTERNAL), null, null);
    }

    @Test
    void enteringAStoreChecksItIsOperableBeforeResolvingItsPod() throws Exception {
        when(internalStoreService.getStorePod(any(), eq(STORE))).thenReturn(POD);
        when(podDirectory.find(POD)).thenReturn(Optional.of(pod()));

        assertThat(routerApi.getStorePodByStoreId(identity(), STORE)).isEqualTo(pod());

        InOrder order = Mockito.inOrder(internalStoreService);
        order.verify(internalStoreService).requireOperable(STORE);
        order.verify(internalStoreService).getStorePod(any(), eq(STORE));
    }

    @Test
    void aStoreBoundToAPodTheRegistryHasForgottenIsATypedNotFound() throws Exception {
        when(internalStoreService.getStorePod(any(), eq(STORE))).thenReturn(POD);
        when(podDirectory.find(POD)).thenReturn(Optional.empty());

        // Not a null the console would render as an empty shop.
        assertThatThrownBy(() -> routerApi.getStorePodByStoreId(identity(), STORE))
                .isInstanceOf(PodNotFoundException.class);
    }

    @Test
    void suspendingWithoutAReasonStillRecordsOne() throws Exception {
        lifecycleApi.suspend(identity(), STORE, null, operator());

        verify(lifecycleService).suspend(any(), eq(STORE), eq(OPERATOR), eq("suspended by operator"));
    }

    @Test
    void anExplicitSuspensionReasonIsKept() throws Exception {
        lifecycleApi.suspend(identity(), STORE, REASON, operator());

        verify(lifecycleService).suspend(any(), eq(STORE), eq(OPERATOR), eq(REASON));
    }

    @Test
    void everyLifecycleTransitionRecordsWhoAskedAndFallsBackToUnknown() throws Exception {
        lifecycleApi.resume(identity(), STORE, operator());
        lifecycleApi.archive(identity(), STORE, null);
        lifecycleApi.delete(identity(), STORE, operator());

        verify(lifecycleService).resume(any(), eq(STORE), eq(OPERATOR));
        verify(lifecycleService).archive(any(), eq(STORE), eq(UNKNOWN_ACTOR));
        verify(lifecycleService).delete(any(), eq(STORE), eq(OPERATOR));
    }

    @Test
    void theNameUniquenessCheckAnswersAsAKeyedFlagTheConsoleCanRead() {
        when(internalStoreService.checkNameExists(STORE_NAME)).thenReturn(true);

        assertThat(storeManagerApi.checkExist(STORE_NAME)).containsEntry("exists", true);
    }

    @Test
    void theDetailedListingUsesAnEmptyQueryRatherThanNoQuery() {
        when(internalStoreService.findAll(any(), any(), any())).thenReturn(Page.empty());

        storeManagerApi.findAllStoresDetailed(identity(), PageRequest.of(0, 20));

        verify(internalStoreService).findAll(any(), eq(new ListManagerStoreQuery(null, null, null, null)), any());
    }

    @Test
    void listingCreatingAndReadingAStoreAllPassTheIdentityThrough() throws Exception {
        ListManagerStoreQuery query = new ListManagerStoreQuery(null, null, null, null);
        when(internalStoreService.findAll(any(), any(), any())).thenReturn(Page.empty());

        storeManagerApi.findAllStores(identity(), query, PageRequest.of(0, 20));
        storeManagerApi.storesPerPod();
        storeManagerApi.getStoreDetailed(identity(), STORE);
        storeManagerApi.storeInfo(identity(), STORE);

        verify(internalStoreService).findAll(any(), eq(query), any());
        verify(internalStoreService).storesPerPod();
        verify(managerService).getStore(any(), eq(STORE));
        verify(internalStoreService).findStore(any(), eq(STORE));
    }

    @Test
    void aStoreIsCreatedForTheCallersOwnOrganizationRatherThanOneNamedInTheBody() throws Exception {
        // The org comes from the token, never the request body: taking it from the body would let an org admin
        // create a store inside somebody else's organisation.
        storeManagerApi.create(identity(), null);

        verify(managerService).createStore(eq(ORG), any());
    }

    @Test
    void thePublicCatalogueEndpointsAnswerFromTheEnumsThemselves() {
        assertThat(storeManagerApi.themes()).isEqualTo(Theme.getImplementedThemes());
        assertThat(storeManagerApi.colorThemes()).isEqualTo(ColorTheme.values());
        assertThat(storeManagerApi.socialLinkProviders()).isEqualTo(SocialProvider.values());
    }

    @Test
    void thoseCatalogueEndpointsAreDeliberatelyUngatedBecauseTheyAreTheSameForEveryStore() {
        assertThat(Map.of("themes", "public/themes", "colorThemes", "public/color-themes",
                "socialLinkProviders", "public/social-links-providers")).hasSize(3);
    }
}
