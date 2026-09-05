package com.asrevo.cvhome.checkout.api.v1.customer;

import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.model.customer.CustomerFilter;
import com.asrevo.cvhome.checkout.services.customer.CustomerService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The console's customer list.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Customers (console)")
@RequiredArgsConstructor
public class CustomerAdminApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')";

    private final CustomerService customers;

    @GetMapping("/private/customers")
    @PreAuthorize(MANAGE)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableCustomerList list(@RequestParam(required = false) String name,
                                     @RequestParam(required = false) String firstName,
                                     @RequestParam(required = false) String lastName,
                                     @RequestParam(required = false) String email,
                                     @RequestParam(required = false) String country,
                                     StoreMerchantId merchantStore, LanguageCode language, Pageable pageable) {
        return customers.list(merchantStore, new CustomerFilter(name, firstName, lastName, email, country), pageable);
    }
}
