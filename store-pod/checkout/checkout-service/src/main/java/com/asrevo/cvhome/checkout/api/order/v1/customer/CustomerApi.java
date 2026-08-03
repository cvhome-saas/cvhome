package com.asrevo.cvhome.checkout.api.order.v1.customer;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.errors.CustomerNotFoundException;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.store.core.constants.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Customer management resource", description = "Manage customers")
public class CustomerApi {

    private final CustomerFacade customerFacade;

    public CustomerApi(CustomerFacade customerFacade) {
        this.customerFacade = customerFacade;
    }

    /**
     * Get all customers
     */
    @GetMapping("/private/customers")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public ReadableCustomerList list(StoreMerchantId merchantStore, LanguageCode language, Pageable pageable) {
        CustomerCriteria customerCriteria = new CustomerCriteria();
        customerCriteria.setPageable(pageable);
        return customerFacade.getListByStore(merchantStore, customerCriteria, LanguageCode.nonLanguage());
    }

    @GetMapping(value = {"/private/customer/info"})
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUSTOMER.*')")
    public ReadableCustomer getCustomerInfo(StoreMerchantId merchantStore, LanguageCode language,
                                            JwtAuthenticationToken auth) throws CustomerNotFoundException {

        String cuaExternalId = (String) auth.getTokenAttributes().get("sub");
        // Not a map(...): the lookup names a checked condition now, and a Function cannot carry one.
        Optional<ReadableCustomer> known = customerFacade.getCustomerByCuaExternalId(cuaExternalId);
        if (known.isEmpty()) {
            return new ReadableCustomer();
        }
        return customerFacade.getCustomerById(known.get().getId(), merchantStore, language);
    }

}
