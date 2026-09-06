package com.asrevo.cvhome.checkout.services.order;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.entity.Orders;
import com.asrevo.cvhome.checkout.errors.OrderLoginRequiredException;
import com.asrevo.cvhome.checkout.model.order.PlaceOrderRequest;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.services.store.StoreSettings;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * The placement facade: login rule first, then the transaction, then up to three remote steps, then the confirmation.
 */
@ExtendWith(MockitoExtension.class)
class OrderPlacementServiceImplTest {

    private static final CartCode CODE = CartCode.of("cart-1");

    private static final RedirectUrls URLS = new RedirectUrls(Orders.SUCCESS_URL, Orders.CANCEL_URL);

    @Mock
    private OrderPlacementTransaction transaction;

    @Mock
    private StoreSettings storeSettings;

    @Mock
    private OrderStepRunner steps;

    @InjectMocks
    private OrderPlacementServiceImpl service;

    @Test
    void placesThenRunsThreeStepsAndAnswersTheConfirmation() throws Exception {
        PlaceOrderRequest request = OrderPlacementTransactionTest.request(PaymentType.STRIPE);
        ShopperId shopper = new ShopperId("sub-1");
        when(transaction.createOrResume(Orders.STORE, LanguageCode.defaultLanguage(), CODE, request, shopper, URLS))
                .thenReturn(100L);
        when(storeSettings.locale(any())).thenReturn(Locale.US);
        when(transaction.confirmation(100L, Locale.US))
                .thenReturn(OrderMapper.toConfirmation(Orders.awaitingPayment(PaymentType.STRIPE), Locale.US));

        ReadableOrderConfirmation confirmation = service.place(Orders.STORE, LanguageCode.defaultLanguage(), CODE,
                request, shopper, URLS);

        assertThat(confirmation.getId()).isEqualTo(100L);
        assertThat(confirmation.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(confirmation.getRedirectUrl()).isEqualTo("https://pay/redirect");
        assertThat(confirmation.getTotal().getGrandTotal()).isEqualTo("$20.00");
        verify(steps).runUntilSettled(100L, 3);
        verify(storeSettings, never()).requiresLogin(any());
    }

    @Test
    void aGuestIsAllowedWhenTheStoreDoesNotRequireLogin() throws Exception {
        PlaceOrderRequest request = OrderPlacementTransactionTest.request(PaymentType.COD);
        when(storeSettings.requiresLogin(Orders.STORE)).thenReturn(false);
        when(transaction.createOrResume(eq(Orders.STORE), any(), eq(CODE), eq(request), eq(null), eq(URLS)))
                .thenReturn(100L);
        when(storeSettings.locale(any())).thenReturn(Locale.US);
        when(transaction.confirmation(100L, Locale.US))
                .thenReturn(OrderMapper.toConfirmation(Orders.paid(PaymentType.COD), Locale.US));

        ReadableOrderConfirmation confirmation = service.place(Orders.STORE, null, CODE, request, null, URLS);

        assertThat(confirmation.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(confirmation.getRedirectUrl()).isNull();
    }

    @Test
    void aGuestIsRefusedWhenTheStoreRequiresLogin() {
        when(storeSettings.requiresLogin(Orders.STORE)).thenReturn(true);

        assertThatThrownBy(() -> service.place(Orders.STORE, null, CODE,
                OrderPlacementTransactionTest.request(PaymentType.COD), null, URLS))
                .isInstanceOf(OrderLoginRequiredException.class);
        verifyNoInteractions(transaction, steps);
    }
}
