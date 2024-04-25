package com.asrevo.cvhome.store.controller.v1.customer;


import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.store.service.facade.customer.CustomerFacade;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_STORE;

@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Customer management resource", description = "Manage customers")
public class CustomerApi {

    @Autowired
    private CustomerFacade customerFacade;


    /**
     * Get all customers
     *
     * @param count
     * @return
     * @throws Exception
     */
    @GetMapping("/private/customers")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCustomerList list(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "count", required = false) Integer count,
                                     @Parameter(hidden = true) MerchantStore merchantStore,
                                     @Parameter(hidden = true) Language language) {
        CustomerCriteria customerCriteria = createCustomerCriteria(page, count);
        return customerFacade.getListByStore(merchantStore, customerCriteria, language);
    }

    private CustomerCriteria createCustomerCriteria(Integer start, Integer count) {
        CustomerCriteria customerCriteria = new CustomerCriteria();
        Optional.ofNullable(start).ifPresent(customerCriteria::setStartIndex);
        Optional.ofNullable(count).ifPresent(customerCriteria::setMaxCount);
        return customerCriteria;
    }


}
