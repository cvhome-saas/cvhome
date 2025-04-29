package com.asrevo.cvhome.order.api.order.v1.customer;


import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.order.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.order.service.facade.customer.CustomerFacade;
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

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Customer management resource", description = "Manage customers")
public class CustomerApi {

    @Autowired
    private CustomerFacade customerFacade;


    /**
     * Get all customers
     */
    @GetMapping("/private/customers")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ConditionalOnApiStatus
    public ReadableCustomerList list(@RequestParam(value = "page", required = false) Integer page,
                                     @RequestParam(value = "count", required = false) Integer count,
                                     @Parameter(hidden = true) StoreMerchantId merchantStore,
                                     @Parameter(hidden = true) LanguageCode language) {
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
