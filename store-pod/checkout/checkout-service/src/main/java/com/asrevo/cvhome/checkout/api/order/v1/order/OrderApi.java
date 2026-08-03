package com.asrevo.cvhome.checkout.api.order.v1.order;

import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableAnonymousOrder;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderStatus;
import com.asrevo.cvhome.checkout.service.facade.checkout.OrderPlacementFacade;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.checkout.service.facade.order.model.OrderProcessingResult;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.checkout.utils.DomainResolver;
import com.asrevo.cvhome.checkout.utils.RedirectUriConfirmation;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.UnsupportedCountryCodeException;
import com.asrevo.cvhome.customer.errors.UnsupportedZoneCodeException;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.payment.api.errors.PaymentApiUnavailableException;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.utils.LocaleUtils;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order flow resource", description = "Manage orders (create, list, get)")
@Slf4j
public class OrderApi {

    private static final String CLIENT_ID_ATTRIBUTE = "clientId";

    private static final String INVALID_CLIENT_ID_MESSAGE = "HTTP 401 Unauthorized - Invalid clientId";

    private final OrderFacade orderFacade;

    private final OrderPlacementFacade orderPlacementFacade;

    private final ShoppingCartService shoppingCartService;

    private final CustomerFacade customerFacade;

    private final ExternalMerchantStoreService externalMerchantStoreService;

    public OrderApi(OrderFacade orderFacade, OrderPlacementFacade orderPlacementFacade, ShoppingCartService shoppingCartService,
                    CustomerFacade customerFacade,
                    ExternalMerchantStoreService externalMerchantStoreService) {
        this.orderFacade = orderFacade;
        this.orderPlacementFacade = orderPlacementFacade;
        this.shoppingCartService = shoppingCartService;
        this.customerFacade = customerFacade;
        this.externalMerchantStoreService = externalMerchantStoreService;
    }

    /**
     * Main checkout resource that will complete the order flow
     */
    @PostMapping(value = {"/cart/{code}/checkout"})
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    public ReadableOrderConfirmation checkout(@PathVariable String code,
                                              @Valid @RequestBody PersistableAnonymousOrder order, JwtAuthenticationToken auth,
                                              StoreMerchantId merchantStore, LanguageCode language, HttpServletRequest request)
            throws ServiceException, PaymentApiUnavailableException, UnsupportedCountryCodeException,
            UnsupportedZoneCodeException {

        ShoppingCart cart;
        ReadableMerchantStore store = externalMerchantStoreService.getStore(merchantStore);

        if (store.isRequireLoginForOrderPlacement()) {
            if (auth == null || !auth.isAuthenticated()) {
                throw new ServiceRuntimeException("HTTP 401 Unauthorized - Login required for order placement");
            }
            if (!merchantStore.getId().equals(auth.getTokenAttributes().get(CLIENT_ID_ATTRIBUTE))) {
                throw new ServiceRuntimeException(INVALID_CLIENT_ID_MESSAGE);
            }
        }

        cart = shoppingCartService.loadCartByCode(code, merchantStore, language);

        if (cart == null) {
            throw new ResourceNotFoundException(String.format("Cart code %s does not exist", code));
        } else {
            order.setShoppingCartId(cart.getId());
        }

        Optional.ofNullable(auth)
                .filter(JwtAuthenticationToken::isAuthenticated)
                .flatMap(token -> Optional.ofNullable(token.getTokenAttributes().get("sub")))
                .map(Object::toString)
                .ifPresent(it -> order.getCustomer().setCuaExternalId(it));

        Customer customer = customerFacade.getOrCreateCustomer(order.getCustomer(), merchantStore, language)
                .orElseThrow(() -> new ServiceRuntimeException(
                        String.format("Unable to create or retrieve customer for cart placement %s",
                                cart.getCustomerId())));

        String domain = new DomainResolver(request).domain();


        RedirectUriConfirmation redirectUriConfirmation = new RedirectUriConfirmation(domain, language);
        OrderProcessingResult processOrder = orderPlacementFacade.placeOrder(order, customer, merchantStore, language,
                LocaleUtils.getLocale(language), redirectUriConfirmation.success(), redirectUriConfirmation.cancel());

        ReadableOrderConfirmation orderConfirmation = orderFacade.orderConfirmation(processOrder.order(), customer, merchantStore,
                language);
        orderConfirmation.setRedirectUrl(processOrder.redirectUrl());

        return orderConfirmation;

    }


    /**
     * Status lookup used by the checkout success/cancel redirect pages. Mirrors the same login
     * requirement as placing the order: if the store requires login for order placement, the
     * caller must be authenticated to view the status too; otherwise it's public.
     */
    @GetMapping(value = {"/order/{orderId}/status"})
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrderStatus orderStatus(@PathVariable Long orderId, JwtAuthenticationToken auth, StoreMerchantId merchantStore) {
        ReadableMerchantStore store = externalMerchantStoreService.getStore(merchantStore);

        if (store.isRequireLoginForOrderPlacement()) {
            if (auth == null || !auth.isAuthenticated()) {
                throw new ServiceRuntimeException("HTTP 401 Unauthorized - Login required to view order status");
            }
            if (!merchantStore.getId().equals(auth.getTokenAttributes().get(CLIENT_ID_ATTRIBUTE))) {
                throw new ServiceRuntimeException(INVALID_CLIENT_ID_MESSAGE);
            }
        }

        return orderFacade.getOrderStatus(orderId, merchantStore);
    }

    @GetMapping(value = {"/private/orders"})
    @ResponseStatus(HttpStatus.OK)

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public ReadableOrderList list(@RequestParam(value = "name", required = false) String name,
                                  @RequestParam(value = "id", required = false) Long id,
                                  @RequestParam(value = "status", required = false) String status,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  @RequestParam(value = "email", required = false) String email, StoreMerchantId merchantStore,
                                  LanguageCode language, Pageable pageable) {

        OrderCriteria orderCriteria = new OrderCriteria();
        orderCriteria.setPageable(pageable);

        orderCriteria.setCustomerName(name);
        orderCriteria.setCustomerPhone(phone);
        orderCriteria.setStatus(status);
        orderCriteria.setEmail(email);
        orderCriteria.setId(id);
        return orderFacade.getReadableOrderList(orderCriteria, merchantStore);
    }

    @GetMapping(value = {"/private/orders/{id}"})
    @ResponseStatus(HttpStatus.OK)

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public ReadableOrder get(@PathVariable final Long id, StoreMerchantId merchantStore, LanguageCode language) {

        return orderFacade.getReadableOrder(id, merchantStore, language);
    }

}
