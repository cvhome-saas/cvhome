package com.asrevo.cvhome.store.controller.v1.order;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartItem;
import com.asrevo.cvhome.store.core.model.order.OrderSummary;
import com.asrevo.cvhome.store.core.model.order.OrderTotalSummary;
import com.asrevo.cvhome.store.core.model.order.ReadableOrderTotalSummary;
import com.asrevo.cvhome.store.core.model.shipping.ShippingSummary;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.order.OrderService;
import com.asrevo.cvhome.store.core.services.shipping.ShippingQuoteService;
import com.asrevo.cvhome.store.service.facade.cart.ShoppingCartFacade;
import com.asrevo.cvhome.store.service.populator.order.ReadableOrderSummaryPopulator;
import com.asrevo.cvhome.store.utils.LabelUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Order Total resource", description = "Calculates order total for a giben shopping cart")
@Slf4j
public class OrderTotalApi {

    private final ShoppingCartFacade shoppingCartFacade;

    private final LabelUtils messages;

    private final PricingService pricingService;


    private final ShippingQuoteService shippingQuoteService;

    private final OrderService orderService;

    public OrderTotalApi(ShoppingCartFacade shoppingCartFacade, LabelUtils messages, PricingService pricingService, ShippingQuoteService shippingQuoteService, OrderService orderService) {
        this.shoppingCartFacade = shoppingCartFacade;
        this.messages = messages;
        this.pricingService = pricingService;
        this.shippingQuoteService = shippingQuoteService;
        this.orderService = orderService;
    }


    /**
     * Public api
     *
     * @param quote
     * @param merchantStore
     * @param language
     * @param response
     * @return
     */
    @RequestMapping(
            value = {"/cart/{code}/total"},
            method = RequestMethod.GET)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableOrderTotalSummary calculateTotal(
            @PathVariable final String code,
            @RequestParam(value = "quote", required = false) Long quote,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,//possible postal code, province and country
            HttpServletResponse response) {

        try {
            ShoppingCart shoppingCart = shoppingCartFacade.getShoppingCartModel(code, merchantStore);

            if (shoppingCart == null) {

                response.sendError(404, "Cart code " + code + " does not exist");

                return null;
            }

            ShippingSummary shippingSummary = null;

            // get shipping quote if asked for
            if (quote != null) {
                shippingSummary = shippingQuoteService.getShippingSummary(quote, merchantStore);
            }

            OrderTotalSummary orderTotalSummary = null;

            OrderSummary orderSummary = new OrderSummary();
            orderSummary.setShippingSummary(shippingSummary);
            List<ShoppingCartItem> itemsSet =
                    new ArrayList<ShoppingCartItem>(shoppingCart.getLineItems());
            orderSummary.setProducts(itemsSet);

            orderTotalSummary = orderService.caculateOrderTotal(orderSummary, merchantStore, language);

            ReadableOrderTotalSummary returnSummary = new ReadableOrderTotalSummary();
            ReadableOrderSummaryPopulator populator = new ReadableOrderSummaryPopulator();
            populator.setMessages(messages);
            populator.setPricingService(pricingService);
            populator.populate(orderTotalSummary, returnSummary, merchantStore, language);

            return returnSummary;

        } catch (Exception e) {
            log.error("Error while calculating order summary", e);
            try {
                response.sendError(503, "Error while calculating order summary " + e.getMessage());
            } catch (Exception ignore) {
            }
            return null;
        }
    }
}
