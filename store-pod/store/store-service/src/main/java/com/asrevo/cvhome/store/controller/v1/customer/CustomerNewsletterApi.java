package com.asrevo.cvhome.store.controller.v1.customer;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.optin.PersistableCustomerOptin;
import com.asrevo.cvhome.store.service.facade.customer.CustomerFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// @TODO ASHRAF

/**
 * Optin a customer to newsletter
 *
 * @author carlsamson
 */
@RestController
@RequestMapping(
        value = "/api/v1",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(
        name = "Manage customer subscription to newsletter",
        description = "Manage customer subscription to newsletter")
public class CustomerNewsletterApi {

    private final CustomerFacade customerFacade;

    public CustomerNewsletterApi(CustomerFacade customerFacade) {
        this.customerFacade = customerFacade;
    }

    /**
     * Create new optin
     */
    @PostMapping("/newsletter")
    @Operation(method = "POST", description = "Creates a newsletter optin")
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void create(
            @Valid @RequestBody PersistableCustomerOptin optin,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        customerFacade.optinCustomer(optin, merchantStore);
    }
}
