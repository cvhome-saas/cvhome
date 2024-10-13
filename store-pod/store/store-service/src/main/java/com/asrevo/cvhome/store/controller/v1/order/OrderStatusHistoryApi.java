package com.asrevo.cvhome.store.controller.v1.order;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.store.core.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.store.service.facade.order.OrderFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order status history resource", description = "Related to OrderManagement api")
public class OrderStatusHistoryApi {

    @Autowired private OrderFacade orderFacade;

    @RequestMapping(
            value = {"private/orders/{id}/history"},
            method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
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
    public List<ReadableOrderStatusHistory> list(
            @PathVariable final Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        //        String user = authorizationUtils.authenticatedUser();
        //        authorizationUtils.authorizeUser(user, Stream.of(Constants.GROUP_SUPERADMIN,
        // Constants.GROUP_ADMIN,
        //                Constants.GROUP_ADMIN_ORDER,
        // Constants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()), merchantStore);

        return orderFacade.getReadableOrderHistory(id, merchantStore, language);
    }

    @RequestMapping(
            value = {"private/orders/{id}/history"},
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Operation(
            method = "POST",
            description = "Add order history",
            summary = "Adds a new status to an order")
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
            @PathVariable final Long id,
            @RequestBody PersistableOrderStatusHistory history,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        //        String user = authorizationUtils.authenticatedUser();
        //        authorizationUtils.authorizeUser(user, Stream.of(Constants.GROUP_SUPERADMIN,
        // Constants.GROUP_ADMIN,
        //                Constants.GROUP_ADMIN_ORDER,
        // Constants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()), merchantStore);

        // TODO validate date format

        orderFacade.createOrderStatus(history, id, merchantStore);
    }
}
