package com.asrevo.cvhome.payment.api.v1.payment;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.controller.v1.auth.AuthController;
import com.asrevo.cvhome.payment.models.TransactionSearchFilter;
import com.asrevo.cvhome.payment.service.PaymentApprovalService;
import com.asrevo.cvhome.payment.service.PaymentConfigurationService;
import com.asrevo.cvhome.payment.service.TransactionService;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import io.namastack.outbox.Outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The payment endpoints: their gates, their store scoping, and the one path pair nothing else checks.
 *
 * <p>
 * {@link ExternalPaymentGatewayApi} is the server half of {@link ExternalPaymentGatewayService}, which is a separate
 * {@code @HttpExchange} interface — so the two paths agree only by hand. They have already drifted once: the status
 * mapping had lost its {@code /private} segment, and no caller noticed because nothing calls status() yet. The last
 * test here is the check the comment on that method asks for.
 * </p>
 */
class PaymentApisTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");
    private static final String ANY_PATH_VARIABLE = "{}";
    private static final String PATH_VARIABLE_PATTERN = "\\{[^}]+}";
    private static final String SUPPORTED_PREFIX = "getSupported";
    private static final String INITIATE = "initiatePayment";
    private static final String STATUS = "status";
    private static final String INTERNAL_REF = "int-1";
    private static final String STORE_ID = STORE.storeMerchantId();

    private final PaymentConfigurationService configurationService =
            Mockito.mock(PaymentConfigurationService.class);
    private final PaymentApprovalService approvalService = Mockito.mock(PaymentApprovalService.class);
    private final TransactionService transactionService = Mockito.mock(TransactionService.class);
    private final Outbox outbox = Mockito.mock(Outbox.class);

    private final PaymentConfigurationController configurationController =
            new PaymentConfigurationController(configurationService);
    private final PublicPaymentConfigurationController publicConfigurationController =
            new PublicPaymentConfigurationController(configurationService);
    private final PrivatePaymentApi privatePaymentApi = new PrivatePaymentApi(approvalService, transactionService);
    private final PublicPaymentWebhookApi webhookApi = new PublicPaymentWebhookApi(outbox);
    private final AuthController authController = new AuthController();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private static JwtAuthenticationToken token() {
        Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject("a-principal")
                .issuedAt(Instant.EPOCH).expiresAt(Instant.EPOCH.plusSeconds(3600)).build();
        return new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("SCOPE_store_pod")));
    }

    @Test
    void readingAndWritingConfigurationAllPassTheStoreThrough() throws Exception {
        when(configurationService.getConfigs(STORE)).thenReturn(List.of());

        configurationController.getConfigs(STORE);
        configurationController.saveConfig(STORE, null);
        configurationController.updateConfig(STORE, PaymentType.STRIPE, null);
        configurationController.deleteConfig(STORE, PaymentType.STRIPE);

        verify(configurationService).getConfigs(STORE);
        verify(configurationService).saveConfig(STORE, null);
        verify(configurationService).updateConfig(STORE, PaymentType.STRIPE, null);
        verify(configurationService).deleteConfig(STORE, PaymentType.STRIPE);
    }

    @Test
    void theSupportedTypeAndStatusListsAreTheEnumsThemselves() {
        assertThat(configurationController.getSupportedPaymentTypes()).isEqualTo(PaymentType.values());
        assertThat(configurationController.getSupportedPaymentStatuses()).isNotEmpty();
    }

    @Test
    void theStorefrontAsksWhichTypesAStoreAcceptsByStoreIdInThePath() {
        // Public: the shopper has no token, so the store arrives in the path rather than from the resolver.
        when(configurationService.getSupportedPaymentTypes(STORE)).thenReturn(new PaymentType[]{PaymentType.STRIPE});

        assertThat(publicConfigurationController.getSupportedPaymentTypes(STORE_ID))
                .containsExactly(PaymentType.STRIPE);
        verify(configurationService).getSupportedPaymentTypes(STORE);
    }

    @Test
    void listingApprovingAndRejectingAreAllScopedToTheStore() {
        TransactionSearchFilter filter = new TransactionSearchFilter(null, null, null, null, null, null);
        privatePaymentApi.list(STORE, filter, PageRequest.of(0, 20));
        privatePaymentApi.reject(STORE, INTERNAL_REF);

        verify(transactionService).list(STORE, filter, PageRequest.of(0, 20));
        verify(approvalService).rejectPayment(STORE, INTERNAL_REF);
    }

    @Test
    void aWebhookIsScheduledOnTheOutboxRatherThanHandledInline() {
        // Handling it inline would make the provider's retry policy our availability policy.
        webhookApi.webhook(STORE_ID, PaymentType.STRIPE, ANY_PATH_VARIABLE, Map.of("stripe-signature", "sig"));

        verify(outbox).schedule(Mockito.any());
    }

    @Test
    void theCurrentPrincipalEndpointAnswersWithTheJwtItWasGiven() {
        JwtAuthenticationToken authentication = token();

        assertThat(authController.current(authentication).getBody())
                .isSameAs(authentication.getPrincipal());
        assertThat(authController.current(authentication).getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void theMeEndpointReadsWhoeverIsInTheSecurityContext() {
        JwtAuthenticationToken authentication = token();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertThat(authController.me()).isSameAs(authentication);
    }

    private static boolean isStoreScopedEndpoint(Method method) {
        return List.of("getConfigs", "saveConfig", "updateConfig", "deleteConfig", "list", "approve", "reject")
                .contains(method.getName());
    }

    private static Stream<Method> privateEndpoints() {
        return Stream.concat(Stream.of(PaymentConfigurationController.class.getDeclaredMethods()),
                        Stream.of(PrivatePaymentApi.class.getDeclaredMethods()))
                .filter(PaymentApisTest::isStoreScopedEndpoint)
                .filter(m -> !m.getName().startsWith(SUPPORTED_PREFIX))
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("privateEndpoints")
    void everyPrivateEndpointCarriesItsPermissionToken(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s.%s has no @PreAuthorize", endpoint.getDeclaringClass().getSimpleName(),
                endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains("'StoreMerchantId','STORE-POD.PAYMENT.*'");
    }

    @Test
    void theSupportedTypeAndStatusListsAreDeliberatelyUngated() {
        // They are the same two enums for every store, so there is nothing tenant-scoped to protect.
        assertThat(Stream.of(PaymentConfigurationController.class.getDeclaredMethods())
                .filter(m -> m.getName().startsWith(SUPPORTED_PREFIX))
                .noneMatch(m -> m.isAnnotationPresent(PreAuthorize.class))).isTrue();
    }

    @Test
    void theGatewayServerPathsMatchTheClientInterfaceTheyAnswer() throws Exception {
        String clientBase = ExternalPaymentGatewayService.class.getAnnotation(HttpExchange.class).value();
        String serverBase = ExternalPaymentGatewayApi.class.getAnnotation(RequestMapping.class).value()[0];

        String initiateClient = clientBase + ExternalPaymentGatewayService.class
                .getMethod(INITIATE, StoreMerchantId.class,
                        com.asrevo.cvhome.payment.model.payment.PaymentRequest.class)
                .getAnnotation(PostExchange.class).value();
        String initiateServer = serverBase + ExternalPaymentGatewayApi.class
                .getMethod(INITIATE, StoreMerchantId.class,
                        com.asrevo.cvhome.payment.model.payment.PaymentRequest.class)
                .getAnnotation(PostMapping.class).value()[0];

        String statusClient = clientBase + ExternalPaymentGatewayService.class
                .getMethod(STATUS, StoreMerchantId.class, String.class)
                .getAnnotation(GetExchange.class).value();
        String statusServer = serverBase + ExternalPaymentGatewayApi.class
                .getMethod(STATUS, StoreMerchantId.class, String.class)
                .getAnnotation(GetMapping.class).value()[0];

        assertThat(initiateServer).isEqualTo(initiateClient);
        // Path-variable names differ by design ({ref} vs {requestRef}); only the shape has to agree.
        assertThat(statusServer.replaceAll(PATH_VARIABLE_PATTERN, ANY_PATH_VARIABLE))
                .isEqualTo(statusClient.replaceAll(PATH_VARIABLE_PATTERN, ANY_PATH_VARIABLE));
    }
}
