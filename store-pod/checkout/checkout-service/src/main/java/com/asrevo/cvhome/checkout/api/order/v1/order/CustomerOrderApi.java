package com.asrevo.cvhome.checkout.api.order.v1.order;

import java.util.List;
import java.util.function.Supplier;

import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrderList;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.constants.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Customer Order resource", description = "Customer orders")
@Slf4j
public class CustomerOrderApi {

    private static final String SUB_KEY = "sub";

    private final OrderFacade orderFacade;

    private final CustomerFacade customerFacade;

    private static @NonNull Supplier<ResourceNotFoundException> buildCustomerNotFoundException(
            StoreMerchantId merchantStore, String cuaExternalId) {
        return () -> new ResourceNotFoundException(
                String.format("Customer not found for sub : %s in store %s", cuaExternalId, merchantStore));
    }

    @GetMapping(value = {"/private/customer/orders"})
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUSTOMER.*')")
    public ReadableOrderList list(StoreMerchantId merchantStore, LanguageCode language, Pageable pageable,
                                  JwtAuthenticationToken auth) {

        OrderCriteria orderCriteria = new OrderCriteria();
        orderCriteria.setPageable(pageable);

        String cuaExternalId = (String) auth.getTokenAttributes().get(SUB_KEY);
        return customerFacade.getCustomerByCuaExternalId(cuaExternalId).map(it -> {
            orderCriteria.setCustomerId(it.getId());
            return orderCriteria;
        }).map(it -> orderFacade.getReadableOrderList(it, merchantStore)).orElseGet(ReadableOrderList::new);

    }

    @GetMapping(value = {"/private/customer/{id}/order"})
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUSTOMER.*')")
    public ReadableOrder get(@PathVariable final Long id, StoreMerchantId merchantStore, LanguageCode language,
                             JwtAuthenticationToken auth) {
        String cuaExternalId = (String) auth.getTokenAttributes().get(SUB_KEY);
        return customerFacade.getCustomerByCuaExternalId(cuaExternalId)
                .map(it -> orderFacade.getReadableOrder(id, it.getId(), merchantStore, language))
                .orElseThrow(buildCustomerNotFoundException(merchantStore, cuaExternalId));
    }

    @GetMapping(value = {"/private/customer/{id}/order/history"})
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CUSTOMER.*')")
    public List<ReadableOrderStatusHistory> history(@PathVariable final Long id, StoreMerchantId merchantStore,
                                                    LanguageCode language, JwtAuthenticationToken auth) {

        String cuaExternalId = (String) auth.getTokenAttributes().get(SUB_KEY);
        return customerFacade.getCustomerByCuaExternalId(cuaExternalId)
                .map(it -> orderFacade.getReadableOrderHistory(id, it.getId(), merchantStore, language))
                .orElseThrow(buildCustomerNotFoundException(merchantStore, cuaExternalId));
    }

}
