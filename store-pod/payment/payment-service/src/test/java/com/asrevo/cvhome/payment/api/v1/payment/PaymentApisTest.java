package com.asrevo.cvhome.payment.api.v1.payment;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.errors.InvalidWebhookSignatureException;
import com.asrevo.cvhome.payment.errors.PaymentConfigurationNotFoundException;
import com.asrevo.cvhome.payment.models.TransactionSearchFilter;
import com.asrevo.cvhome.payment.service.PaymentApprovalService;
import com.asrevo.cvhome.payment.service.PaymentConfigurationService;
import com.asrevo.cvhome.payment.service.PaymentGatewayService;
import com.asrevo.cvhome.payment.service.TransactionService;
import com.asrevo.cvhome.payment.services.payment.ExternalPaymentGatewayService;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import io.namastack.outbox.Outbox;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
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
 *
 * <p>
 * The gate walk covers every controller in the service rather than a list of method names: the earlier name-filtered
 * source never saw {@code ExternalPaymentGatewayApi}, which is how initiate and status shipped with no gate at all.
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
    private static final String PRIVATE_SEGMENT = "/private/";
    private static final String SIGNATURE_HEADER = "stripe-signature";
    private static final String SIGNATURE = "sig";
    private static final String STRIPE = "stripe";
    private static final String NOT_AN_ID = "not-an-object-id";
    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.PAYMENT.*')";
    private static final String MANAGE_STORE = "hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.*')";
    private static final String GATEWAY = "hasPermission(#store,'StoreMerchantId','STORE-POD.PAYMENT.INITIATE')";
    /**
     * Authenticated by the filter chain but carrying no token: the two enum lists are the same for every store, so
     * there is nothing tenant-scoped for a gate to protect. Anything else under {@code /private/} must be gated.
     */
    private static final Set<String> UNGATED_PRIVATE = Set.of("getSupportedPaymentTypes", "getSupportedPaymentStatuses");

    private final PaymentConfigurationService configurationService =
            Mockito.mock(PaymentConfigurationService.class);
    private final PaymentApprovalService approvalService = Mockito.mock(PaymentApprovalService.class);
    private final TransactionService transactionService = Mockito.mock(TransactionService.class);
    private final Outbox outbox = Mockito.mock(Outbox.class);
    private final PaymentGatewayService gatewayService = Mockito.mock(PaymentGatewayService.class);

    private final PaymentConfigurationController configurationController =
            new PaymentConfigurationController(configurationService);
    private final PublicPaymentConfigurationController publicConfigurationController =
            new PublicPaymentConfigurationController(configurationService);
    private final PrivatePaymentApi privatePaymentApi = new PrivatePaymentApi(approvalService, transactionService);
    private final PublicPaymentWebhookApi webhookApi = new PublicPaymentWebhookApi(outbox, gatewayService);

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
    void aWebhookIsScheduledOnTheOutboxRatherThanHandledInline() throws Exception {
        // Handling it inline would make the provider's retry policy our availability policy.
        Map<String, String> headers = Map.of(SIGNATURE_HEADER, SIGNATURE);

        webhookApi.webhook(STORE_ID, PaymentType.STRIPE, ANY_PATH_VARIABLE, headers);

        // Authenticated first, scheduled second: the signature is the endpoint's only credential.
        var order = Mockito.inOrder(gatewayService, outbox);
        order.verify(gatewayService).authenticateWebhook(STORE, PaymentType.STRIPE, ANY_PATH_VARIABLE, headers);
        order.verify(outbox).schedule(Mockito.any());
    }

    @Test
    void aWebhookThatFailsAuthenticationIsNotScheduledOnTheOutbox() throws Exception {
        // Scheduling first and verifying on the outbox let anyone write a row for any store, and told them nothing.
        doThrow(InvalidWebhookSignatureException.verificationFailed(STRIPE, false, null))
                .doThrow(PaymentConfigurationNotFoundException.of(PaymentType.STRIPE, STORE))
                .when(gatewayService).authenticateWebhook(Mockito.eq(STORE), Mockito.eq(PaymentType.STRIPE),
                        Mockito.anyString(), Mockito.any());

        assertThatThrownBy(() -> webhookApi.webhook(STORE_ID, PaymentType.STRIPE, ANY_PATH_VARIABLE, Map.of()))
                .isInstanceOf(InvalidWebhookSignatureException.class);
        assertThatThrownBy(() -> webhookApi.webhook(STORE_ID, PaymentType.STRIPE, ANY_PATH_VARIABLE, Map.of()))
                .isInstanceOf(PaymentConfigurationNotFoundException.class);

        verifyNoInteractions(outbox);
    }

    @Test
    void aStoreIdThatIsNotAnObjectIdIsNotFoundBeforeAnythingIsLookedUp() {
        // Not a 400: a stranger probing the endpoint learns nothing about which ids are stores.
        assertThatThrownBy(() -> webhookApi.webhook(NOT_AN_ID, PaymentType.STRIPE, ANY_PATH_VARIABLE, Map.of()))
                .isInstanceOf(PaymentConfigurationNotFoundException.class);

        verifyNoInteractions(gatewayService, outbox);
    }

    static Stream<Class<?>> controllers() {
        return Stream.of(PaymentConfigurationController.class, PublicPaymentConfigurationController.class,
                PrivatePaymentApi.class, PublicPaymentWebhookApi.class, ExternalPaymentGatewayApi.class);
    }

    /**
     * Every handler whose path is private carries a {@code @PreAuthorize} for the audience the path implies — the
     * seller's manage token, or the same-pod initiate token on the gateway — and every public handler carries none.
     */
    @ParameterizedTest(name = "{0}")
    @MethodSource("controllers")
    void everyPrivateHandlerIsGatedForItsAudienceAndEveryPublicOneIsOpen(Class<?> controller) {
        RequestMapping base = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
        String prefix = base == null || base.path().length == 0 ? "" : base.path()[0];
        for (Method method : controller.getDeclaredMethods()) {
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (mapping == null) {
                continue;
            }
            String path = prefix + (mapping.path().length == 0 ? "" : mapping.path()[0]);
            PreAuthorize gate = AnnotatedElementUtils.findMergedAnnotation(method, PreAuthorize.class);
            String name = String.format("%s.%s", controller.getSimpleName(), method.getName());
            if (!path.contains(PRIVATE_SEGMENT) || UNGATED_PRIVATE.contains(method.getName())) {
                assertThat(gate).as("%s is open", name).isNull();
                continue;
            }
            assertThat(gate).as("%s must be gated", name).isNotNull();
            String expected = controller == ExternalPaymentGatewayApi.class ? GATEWAY
                    : controller == PrivatePaymentApi.class ? MANAGE_STORE : MANAGE;
            assertThat(gate.value()).as(name).isEqualTo(expected);
        }
    }

    @Test
    void theControllerListIsComplete() {
        // A controller added to the service but not here is a controller the walk never sees.
        assertThat(controllers()).hasSize(5);
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
