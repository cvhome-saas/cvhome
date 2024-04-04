package com.asrevo.cvhome.store.controller.v1.order;

import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.order.Order;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.store.core.model.order.v1.PersistableAnonymousOrder;
import com.asrevo.cvhome.store.core.model.order.v1.ReadableOrderConfirmation;
import com.asrevo.cvhome.store.core.services.customer.CustomerService;
import com.asrevo.cvhome.store.core.services.order.OrderService;
import com.asrevo.cvhome.store.core.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.store.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.store.service.facade.order.OrderFacade;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.beanvalidation.IntegrationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order flow resource", description = "Manage orders (create, list, get)")
@Slf4j
public class OrderApi {


    private static final String DEFAULT_ORDER_LIST_COUNT = "25";
    @Autowired
    private CustomerService customerService;
    @Autowired
    private OrderFacade orderFacade;

/*	@Autowired
	private com.salesmanager.shop.store.controller.order.facade.v1.OrderFacade orderFacadeV1;*/
    @Autowired
    private OrderService orderService;
    @Autowired
    private ShoppingCartService shoppingCartService;
    @Autowired
    private CustomerFacade customerFacade;
    @Autowired
    private CustomerFacade customerFacadev1; //v1 version

    /**
     * Main checkout resource that will complete the order flow
     *
     * @param code
     * @param order
     * @param merchantStore
     * @param language
     * @return
     */
    @RequestMapping(value = {"/cart/{code}/checkout"}, method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })

    public ReadableOrderConfirmation checkout(
            @PathVariable final String code,//shopping cart
            @Valid @RequestBody PersistableAnonymousOrder order,//order
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        Assert.notNull(order.getCustomer(), "Customer must not be null");


        ShoppingCart cart;
        try {
            cart = shoppingCartService.getByCode(code, merchantStore);

            if (cart == null) {
                throw new ResourceNotFoundException("Cart code " + code + " does not exist");
            }


            Customer customer = new Customer();
            customer = customerFacade.populateCustomerModel(customer, order.getCustomer(), merchantStore, language);


            order.setShoppingCartId(cart.getId());

            Order modelOrder = orderFacade.processOrder(order, customer, merchantStore, language,
                    LocaleUtils.getLocale(language));
            Long orderId = modelOrder.getId();
            //populate order confirmation
            order.setId(orderId);
            // set customer id
            order.getCustomer().setId(modelOrder.getCustomerId());

            return orderFacade.orderConfirmation(modelOrder, customer, merchantStore, language);

        } catch (Exception e) {

            String message = e.getMessage();
            if (StringUtils.isBlank(message)) {//exception type
                message = "APP-BACKEND";
                if (e.getCause() instanceof IntegrationException) {
                    message = "Integration problen occured to complete order";
                }
            }
            throw new ServiceRuntimeException("Error during checkout [" + message + "]", e);
        }

    }

}
