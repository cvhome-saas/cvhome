package com.asrevo.cvhome.checkout.api.v1.order;

import java.net.URI;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.asrevo.cvhome.checkout.config.CurrentShopper;
import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.errors.CartAlreadyConvertedException;
import com.asrevo.cvhome.checkout.errors.CartEmptyException;
import com.asrevo.cvhome.checkout.errors.CartNotFoundException;
import com.asrevo.cvhome.checkout.errors.CartQuantityOutOfRangeException;
import com.asrevo.cvhome.checkout.errors.OrderLoginRequiredException;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.errors.ProductNotPurchasableException;
import com.asrevo.cvhome.checkout.model.order.PlaceOrderRequest;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatus;
import com.asrevo.cvhome.checkout.services.order.OrderPlacementService;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.checkout.services.order.RedirectUrls;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.inventory.api.errors.InventoryApiUnavailableException;
import com.asrevo.cvhome.inventory.api.errors.ProductReservationRejectedException;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.payment.api.errors.PaymentGatewayRejectedException;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * Placing an order and reading its status back on the payment-return page. Not under {@code /private} because a store
 * may allow guest checkout; when it does not, the service answers 401 itself.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Checkout")
@RequiredArgsConstructor
public class CheckoutApi {

    private static final String ORIGIN = "Origin";

    private static final String REFERER = "Referer";

    private static final String CHECKOUT = "checkout";

    private final OrderPlacementService placement;

    private final OrderService orders;

    @PostMapping("/cart/{code}/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrderConfirmation checkout(@PathVariable String code, @Valid @RequestBody PlaceOrderRequest request,
                                              StoreMerchantId merchantStore, LanguageCode language,
                                              @CurrentShopper ShopperId shopper, HttpServletRequest http)
            throws CartNotFoundException, CartEmptyException, CartAlreadyConvertedException,
            OrderLoginRequiredException, ProductNotPurchasableException, CartQuantityOutOfRangeException,
            UnsupportedCountryCodeException, ProductReservationRejectedException, InventoryApiUnavailableException,
            PaymentGatewayRejectedException, PaymentApiUnavailableException {
        return placement.place(merchantStore, language, CartCode.of(code), request, shopper,
                redirects(http, language));
    }

    @GetMapping("/order/{orderId}/status")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrderStatus status(@PathVariable Long orderId, StoreMerchantId merchantStore,
                                      @CurrentShopper ShopperId shopper) throws OrderNotFoundException {
        return orders.status(merchantStore, orderId, shopper);
    }

    /**
     * The storefront's origin, which the payment provider sends the shopper back to: {@code Origin}, else the
     * {@code Referer}'s authority, else the request's own host.
     */
    public static RedirectUrls redirects(HttpServletRequest http, LanguageCode language) {
        String domain = http.getHeader(ORIGIN);
        if (domain == null || domain.isBlank()) {
            String referer = http.getHeader(REFERER);
            if (referer != null && !referer.isBlank()) {
                URI uri = URI.create(referer);
                domain = String.format("%s://%s", uri.getScheme(), uri.getAuthority());
            } else {
                domain = UriComponentsBuilder.newInstance().scheme(http.getScheme()).host(http.getServerName())
                        .port(http.getServerPort()).toUriString();
            }
        }
        String lang = language == null ? LanguageCode.defaultLanguage().code() : language.code();
        return new RedirectUrls(
                UriComponentsBuilder.fromUriString(domain).pathSegment(lang, CHECKOUT, "success").toUriString(),
                UriComponentsBuilder.fromUriString(domain).pathSegment(lang, CHECKOUT, "cancel").toUriString());
    }
}
