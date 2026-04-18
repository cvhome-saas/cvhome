package com.asrevo.cvhome.checkout.api.order.v1.order;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.checkout.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order status history resource", description = "Related to OrderManagement api")
public class OrderStatusHistoryApi {

    @Autowired
    private OrderFacade orderFacade;

    @GetMapping(value = {"private/orders/{id}/history"})
    @ResponseStatus(HttpStatus.OK)
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public List<ReadableOrderStatusHistory> list(@PathVariable final Long id, StoreMerchantId merchantStore,
                                                 LanguageCode language) {

        return orderFacade.getReadableOrderHistory(id, merchantStore, language);
    }

    @PostMapping(value = {"private/orders/{id}/history"})
    @ResponseStatus(HttpStatus.CREATED)

    @Operation(method = "POST", description = "Add order history", summary = "Adds a new status to an order")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CHECKOUT.*')")
    public void create(@PathVariable final Long id, @RequestBody PersistableOrderStatusHistory history,
                       StoreMerchantId merchantStore) {

        // TODO validate date format

        orderFacade.createOrderStatus(history, id, merchantStore);
    }

}
