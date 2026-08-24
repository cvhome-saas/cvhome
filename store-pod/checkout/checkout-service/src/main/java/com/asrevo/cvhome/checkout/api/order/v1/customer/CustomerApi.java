package com.asrevo.cvhome.checkout.api.order.v1.customer;

import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
     * Get all customers, optionally filtered.
     *
     * <p>{@code name} is the single "name or email" query a console search box needs: it matches the billing
     * first name, the billing last name or the email address. The remaining four narrow one field each, and
     * are AND-ed with it and with each other — which is why a caller with one search box sends {@code name}
     * alone rather than {@code name} and {@code email} together.
     *
     * <p>Every one of these was already implemented by {@code CustomerRepository.findByStoreMerchantId} and
     * reachable from nowhere: this handler used to build an empty criteria and set only the pageable.
     */
    @GetMapping("/private/customers")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public ReadableCustomerList list(@RequestParam(value = "name", required = false) String name,
                                     @RequestParam(value = "firstName", required = false) String firstName,
                                     @RequestParam(value = "lastName", required = false) String lastName,
                                     @RequestParam(value = "email", required = false) String email,
                                     @RequestParam(value = "country", required = false) String country,
                                     StoreMerchantId merchantStore, LanguageCode language, Pageable pageable) {
        CustomerCriteria customerCriteria = new CustomerCriteria();
        customerCriteria.setPageable(pageable);

        customerCriteria.setName(name);
        customerCriteria.setFirstName(firstName);
        customerCriteria.setLastName(lastName);
        customerCriteria.setEmail(email);
        customerCriteria.setCountry(country);

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
