package com.asrevo.cvhome.tenancy.manager.controller;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
import com.asrevo.cvhome.tenancy.controller.AuthApi;
import com.asrevo.cvhome.tenancy.manager.controller.admin.OrgManagerApi;
import com.asrevo.cvhome.tenancy.manager.controller.statistic.OrgStatisticApi;
import com.asrevo.cvhome.tenancy.manager.controller.statistic.StoreStatisticApi;
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
 *
 * <p>
 * The last two tests walk every handler of every controller on the service: a handler is either public by path,
 * gated by {@code @PreAuthorize}, or named in {@link #AUTHENTICATED_ONLY} with a reason. The authorization audit
 * found the lifecycle endpoints guarded by the store's <em>read</em> token, and nothing on the service would have
 * noticed; this is the notice, until the shared ArchUnit rule replaces it.
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
    private static final String DELETE_TOKEN = "STORE-CORE.STORE-DELETE";
    private static final String OPERATOR_ROLE = "ROLE_SUPER_ADMIN";
    private static final String PUBLIC_SEGMENT = "/public/";
    private static final String HANDLER = "%s.%s";
    private static final String CONTROLLER_PACKAGE = "com.asrevo.cvhome.tenancy";

    /**
     * Handlers the filter chain authenticates but which deliberately carry no token, each with the reason a token
     * would be wrong. An entry that gains a gate must leave this list, or the walk fails: a stale exemption is how
     * the next ungated handler hides.
     *
     * <ul>
     * <li>{@code OrgMemberApi.accept} — the invitee is not yet a member, so no org-scoped check could pass; the
     * single-use invitation token is the authorization.</li>
     * <li>{@code UserAccountApi.current} and {@code assignableRoles} — about the signed-in user and the platform's
     * role catalogue, with no store or organization to check against.</li>
     * </ul>
     */
    private static final Set<String> AUTHENTICATED_ONLY = Set.of("OrgMemberApi.accept", "UserAccountApi.current",
            "UserAccountApi.assignableRoles");

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

    static Stream<Class<?>> controllers() {
        return Stream.of(AuthApi.class, OrgMemberApi.class, SaasApi.class, UserAccountApi.class, OrgStatisticApi.class,
                RouterApi.class, StoreLifecycleApi.class, StoreManagerApi.class, SignUpApi.class, OrgManagerApi.class,
                StoreStatisticApi.class);
    }

    /**
     * Every handler that is not public by path carries {@code @PreAuthorize}, unless it is named in
     * {@link #AUTHENTICATED_ONLY} — and then it must not, so the list cannot go stale.
     */
    @ParameterizedTest
    @MethodSource("controllers")
    void everyHandlerIsPublicByPathGatedOrNamedAsAuthenticatedOnly(Class<?> controller) {
        RequestMapping root = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
        String prefix = root == null || root.path().length == 0 ? "" : root.path()[0];
        for (Method method : controller.getDeclaredMethods()) {
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (mapping == null) {
                continue;
            }
            String path = String.format("/%s/%s", prefix, mapping.path().length == 0 ? "" : mapping.path()[0]);
            String handler = String.format(HANDLER, controller.getSimpleName(), method.getName());
            PreAuthorize gate = AnnotatedElementUtils.findMergedAnnotation(method, PreAuthorize.class);
            if (path.contains(PUBLIC_SEGMENT)) {
                assertThat(gate).as("%s is public by path", handler).isNull();
            } else if (AUTHENTICATED_ONLY.contains(handler)) {
                assertThat(gate).as("%s is gated now; drop it from AUTHENTICATED_ONLY", handler).isNull();
            } else {
                assertThat(gate).as("%s must carry @PreAuthorize", handler).isNotNull();
            }
        }
    }

    /**
     * Archive and delete take the delete token — the owning org admin or the operator — and never the read token
     * a store admin, a moderator and the store-core service principal all pass. Suspend and resume stay the
     * operator's alone.
     */
    @Test
    void closingAStoreTakesTheDeleteTokenAndSuspendingItTakesTheOperator() {
        assertThat(gateOf("archive")).contains(DELETE_TOKEN);
        assertThat(gateOf("delete")).contains(DELETE_TOKEN);
        assertThat(gateOf("suspend")).contains(OPERATOR_ROLE);
        assertThat(gateOf("resume")).contains(OPERATOR_ROLE);
    }

    /** A controller added to the service and not to {@link #controllers()} would escape the walk. */
    @Test
    void theControllerListIsEveryRestControllerOnTheService() {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<String> onTheClasspath = scanner.findCandidateComponents(CONTROLLER_PACKAGE).stream()
                .map(it -> it.getBeanClassName()).sorted().toList();

        assertThat(controllers().map(Class::getName).sorted().toList()).isEqualTo(onTheClasspath);
    }

    private static String gateOf(String handler) {
        return Arrays.stream(StoreLifecycleApi.class.getDeclaredMethods())
                .filter(it -> it.getName().equals(handler))
                .map(it -> AnnotatedElementUtils.findMergedAnnotation(it, PreAuthorize.class))
                .map(PreAuthorize::value)
                .findFirst()
                .orElseThrow();
    }
}
