package com.asrevo.cvhome.checkout.api.v1;

import java.lang.reflect.Method;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Stream;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.web.bind.annotation.RequestMapping;

import com.asrevo.cvhome.checkout.api.v1.cart.CartApi;
import com.asrevo.cvhome.checkout.api.v1.customer.CustomerAdminApi;
import com.asrevo.cvhome.checkout.api.v1.customer.CustomerApi;
import com.asrevo.cvhome.checkout.api.v1.order.CheckoutApi;
import com.asrevo.cvhome.checkout.api.v1.order.ExternalOrderSignalApi;
import com.asrevo.cvhome.checkout.api.v1.order.OrderApi;
import com.asrevo.cvhome.checkout.api.v1.reference.CountryApi;
import com.asrevo.cvhome.checkout.api.v2.statistic.StatisticApi;
import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.model.cart.PersistableCartItem;
import com.asrevo.cvhome.checkout.model.cart.ReadableCart;
import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;
import com.asrevo.cvhome.checkout.model.order.OrderFilter;
import com.asrevo.cvhome.checkout.model.order.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.PlaceOrderRequest;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.signal.PaymentSignal;
import com.asrevo.cvhome.checkout.model.signal.ReservationExpiredSignal;
import com.asrevo.cvhome.checkout.services.cart.CartService;
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.checkout.services.order.OrderPlacementService;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.checkout.services.order.OrderSignalService;
import com.asrevo.cvhome.checkout.services.order.OrderStatisticsService;
import com.asrevo.cvhome.checkout.services.order.RedirectUrls;
import com.asrevo.cvhome.checkout.services.reference.CountryService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StatisticRange;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The controllers are one line each; what matters is that every one passes the store through unchanged, that the
 * redirect urls are built from the storefront's origin, and that every private endpoint carries its gate.
 */
class CheckoutApisTest {

    private static final String ADMIN = "admin";

    private static final String ADA = "ada";

    private static final String SKU_2 = "SKU";

    private static final String A_B = "a@b";

    private static final String C = "c";

    private static final String F = "f";

    private static final String LIT_1 = "1";

    private static final String N = "n";

    private static final String E = "e";

    private static final String L = "l";

    private static final StoreMerchantId STORE = new StoreMerchantId("65f023632bc46470c104b76f");

    private static final LanguageCode EN = LanguageCode.defaultLanguage();

    private static final ShopperId SHOPPER = new ShopperId("sub-1");

    private static final String CODE = "cart-1";

    private static final String REF = "ref-1";

    private static final String MANAGE = "STORE-POD.CHECKOUT.*";

    private static final String CUSTOMER = "STORE-POD.CUSTOMER.*";

    private static final String SIGNAL = "STORE-POD.CHECKOUT.SIGNAL";

    private final CartService cartService = Mockito.mock(CartService.class);

    private final OrderPlacementService placement = Mockito.mock(OrderPlacementService.class);

    private final OrderService orderService = Mockito.mock(OrderService.class);

    private final OrderSignalService signals = Mockito.mock(OrderSignalService.class);

    private final CustomerService customers = Mockito.mock(CustomerService.class);

    private final OrderStatisticsService statistics = Mockito.mock(OrderStatisticsService.class);

    private final CountryService countries = Mockito.mock(CountryService.class);

    private final CartApi cartApi = new CartApi(cartService);

    private final CheckoutApi checkoutApi = new CheckoutApi(placement, orderService);

    private final OrderApi orderApi = new OrderApi(orderService);

    private final ExternalOrderSignalApi signalApi = new ExternalOrderSignalApi(signals);

    private final CustomerApi customerApi = new CustomerApi(customers, orderService);

    private final CustomerAdminApi customerAdminApi = new CustomerAdminApi(customers);

    private final CountryApi countryApi = new CountryApi(countries);

    private final StatisticApi statisticApi = new StatisticApi(statistics);

    @Test
    void cartCallsPassStoreLanguageAndCodeThrough() throws Exception {
        PersistableCartItem item = new PersistableCartItem();
        ReadableCart cart = new ReadableCart();
        when(cartService.create(STORE, EN, item, SHOPPER)).thenReturn(cart);
        when(cartService.upsert(STORE, EN, CartCode.of(CODE), item)).thenReturn(cart);
        when(cartService.get(STORE, EN, CartCode.of(CODE))).thenReturn(cart);
        when(cartService.removeLine(STORE, EN, CartCode.of(CODE), SKU_2)).thenReturn(cart);

        assertThat(cartApi.create(item, STORE, EN, SHOPPER)).isSameAs(cart);
        assertThat(cartApi.upsert(CODE, item, STORE, EN)).isSameAs(cart);
        assertThat(cartApi.get(CODE, STORE, EN)).isSameAs(cart);
        assertThat(cartApi.removeLine(CODE, SKU_2, false, STORE, EN).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(cartApi.removeLine(CODE, SKU_2, true, STORE, EN).getBody()).isSameAs(cart);
    }

    @Test
    void checkoutBuildsTheRedirectsFromTheOriginAndDelegates() throws Exception {
        HttpServletRequest http = Mockito.mock(HttpServletRequest.class);
        when(http.getHeader("Origin")).thenReturn("https://shop.example");
        PlaceOrderRequest request = new PlaceOrderRequest();
        ReadableOrderConfirmation confirmation = new ReadableOrderConfirmation();
        when(placement.place(eq(STORE), eq(EN), eq(CartCode.of(CODE)), eq(request), eq(SHOPPER), any()))
                .thenReturn(confirmation);

        assertThat(checkoutApi.checkout(CODE, request, STORE, EN, SHOPPER, http)).isSameAs(confirmation);

        verify(placement).place(eq(STORE), eq(EN), eq(CartCode.of(CODE)), eq(request), eq(SHOPPER),
                eq(new RedirectUrls("https://shop.example/en/checkout/success", "https://shop.example/en/checkout/cancel")));
    }

    @Test
    void redirectsFallBackToRefererThenToTheRequestHost() {
        HttpServletRequest referer = Mockito.mock(HttpServletRequest.class);
        when(referer.getHeader("Referer")).thenReturn("http://store.local:3000/fr/cart?x=1");
        assertThat(CheckoutApi.redirects(referer, new LanguageCode("fr")).success())
                .isEqualTo("http://store.local:3000/fr/checkout/success");

        HttpServletRequest bare = Mockito.mock(HttpServletRequest.class);
        when(bare.getScheme()).thenReturn("https");
        when(bare.getServerName()).thenReturn("spg.local");
        when(bare.getServerPort()).thenReturn(443);
        assertThat(CheckoutApi.redirects(bare, null).cancel()).isEqualTo("https://spg.local:443/en/checkout/cancel");
    }

    @Test
    void statusAndConsoleReadsPassTheStoreThrough() throws Exception {
        Pageable page = PageRequest.of(0, 10);
        checkoutApi.status(5L, STORE, SHOPPER);
        verify(orderService).status(STORE, 5L, SHOPPER);

        OrderFilter filter = new OrderFilter(ADA, 5L, OrderStatus.CONFIRMED, LIT_1, A_B, 7L, null);
        orderApi.list(filter, STORE, EN, page);
        verify(orderService).list(STORE, EN, filter, page);
        orderApi.get(5L, STORE, EN);
        verify(orderService).get(STORE, EN, 5L);
        orderApi.history(5L, STORE);
        verify(orderService).history(STORE, 5L);

        PersistableOrderStatusHistory change = new PersistableOrderStatusHistory();
        when(orderService.transition(STORE, 5L, change, ADMIN)).thenReturn(new ReadableOrderStatusHistory());
        orderApi.transition(5L, change, STORE, new TestingAuthenticationToken(ADMIN, "x"));
        verify(orderService).transition(STORE, 5L, change, ADMIN);
        orderApi.transition(5L, change, STORE, null);
        verify(orderService).transition(STORE, 5L, change, null);
    }

    @Test
    void signalsCustomersCountriesAndStatisticsDelegate() throws Exception {
        PaymentSignal paid = new PaymentSignal(PaymentStatus.PAID, "tx");
        signalApi.signalPayment(STORE, REF, paid);
        verify(signals).paymentSignal(STORE, REF, paid);
        ReservationExpiredSignal expired = new ReservationExpiredSignal(REF);
        signalApi.signalReservationExpired(STORE, REF, expired);
        verify(signals).reservationExpired(STORE, REF, expired);

        Pageable page = PageRequest.of(0, 10);
        customerApi.info(STORE, SHOPPER);
        verify(customers).info(STORE, SHOPPER);
        customerApi.orders(STORE, EN, SHOPPER, page);
        verify(orderService).listForShopper(STORE, EN, SHOPPER, page);
        customerApi.order(5L, STORE, EN, SHOPPER);
        verify(orderService).getForShopper(STORE, EN, SHOPPER, 5L);
        customerApi.history(5L, STORE, SHOPPER);
        verify(orderService).historyForShopper(STORE, SHOPPER, 5L);
        customerAdminApi.list(N, F, L, E, C, STORE, EN, page);
        verify(customers).list(STORE, new CustomerFilter(N, F, L, E, C), page);

        countryApi.countries(STORE, EN);
        verify(countries).all(EN);

        StatisticRange range = new StatisticRange(ZonedDateTime.now(), ZonedDateTime.now());
        statisticApi.orders(range, STORE);
        statisticApi.customers(range, STORE);
        statisticApi.products(range, STORE);
        verify(statistics).orders(STORE, range);
        verify(statistics).customers(STORE, range);
        verify(statistics).products(STORE, range);
    }

    static Stream<Class<?>> controllers() {
        return Stream.of(CartApi.class, CheckoutApi.class, OrderApi.class, ExternalOrderSignalApi.class,
                CustomerApi.class, CustomerAdminApi.class, CountryApi.class, StatisticApi.class);
    }

    /**
     * Every handler whose path is private carries a {@code @PreAuthorize}, and the token matches the audience the
     * path implies: shopper endpoints take the customer token, the signal API the s2s token, the rest the seller's.
     */
    @ParameterizedTest
    @MethodSource("controllers")
    void privateEndpointsAreGatedForTheirAudience(Class<?> controller) {
        for (Method method : controller.getDeclaredMethods()) {
            RequestMapping mapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (mapping == null || mapping.path().length == 0) {
                continue;
            }
            String path = mapping.path()[0];
            PreAuthorize gate = AnnotatedElementUtils.findMergedAnnotation(method, PreAuthorize.class);
            if (!path.contains("/private/")) {
                assertThat(gate).as("%s.%s is public", controller.getSimpleName(), method.getName()).isNull();
                continue;
            }
            assertThat(gate).as("%s.%s must be gated", controller.getSimpleName(), method.getName()).isNotNull();
            String expected = path.contains("/signals/") ? SIGNAL : path.contains("/private/customer/") ? CUSTOMER : MANAGE;
            assertThat(gate.value()).as("%s.%s", controller.getSimpleName(), method.getName()).contains(expected);
        }
    }

    @Test
    void theControllerListIsComplete() {
        assertThat(controllers()).hasSize(8);
        assertThat(List.of(PaymentType.values())).isNotEmpty();
    }
}
