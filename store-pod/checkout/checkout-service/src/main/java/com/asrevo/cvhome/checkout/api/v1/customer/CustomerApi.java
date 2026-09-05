package com.asrevo.cvhome.checkout.api.v1.customer;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.config.CurrentShopper;
import com.asrevo.cvhome.checkout.domain.ShopperId;
import com.asrevo.cvhome.checkout.errors.OrderNotFoundException;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.checkout.services.order.OrderService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The signed-in shopper's own view: their profile and their orders. {@code STORE-POD.CUSTOMER.*} admits only a shopper
 * token of this store's realm, so a seller cannot read these and a shopper of another store gets 403.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Customer (shopper)")
@RequiredArgsConstructor
public class CustomerApi {

    private static final String SHOPPER = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUSTOMER.*')";

    private final CustomerService customers;

    private final OrderService orders;

    @GetMapping("/private/customer/info")
    @PreAuthorize(SHOPPER)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableCustomer info(StoreMerchantId merchantStore, @CurrentShopper ShopperId shopper) {
        return customers.info(merchantStore, shopper);
    }

    @GetMapping("/private/customer/orders")
    @PreAuthorize(SHOPPER)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrderList orders(StoreMerchantId merchantStore, LanguageCode language,
                                    @CurrentShopper ShopperId shopper, Pageable pageable) {
        return orders.listForShopper(merchantStore, language, shopper, pageable);
    }

    @GetMapping("/private/customer/{id}/order")
    @PreAuthorize(SHOPPER)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableOrderConfirmation order(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language,
                                           @CurrentShopper ShopperId shopper) throws OrderNotFoundException {
        return orders.getForShopper(merchantStore, language, shopper, id);
    }

    @GetMapping("/private/customer/{id}/order/history")
    @PreAuthorize(SHOPPER)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<ReadableOrderStatusHistory> history(@PathVariable Long id, StoreMerchantId merchantStore,
                                                    @CurrentShopper ShopperId shopper) throws OrderNotFoundException {
        return orders.historyForShopper(merchantStore, shopper, id);
    }
}
