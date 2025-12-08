package com.asrevo.cvhome.checkout.api.order.v1.order;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCart;
import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrderList;
import com.asrevo.cvhome.checkout.model.order.v1.PersistableAnonymousOrder;
import com.asrevo.cvhome.checkout.model.order.v1.ReadableOrderConfirmation;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.checkout.services.shoppingcart.ShoppingCartService;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.utils.LocaleUtils;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.beanvalidation.IntegrationException;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Order flow resource", description = "Manage orders (create, list, get)")
@Slf4j
public class OrderApi {

	private final OrderFacade orderFacade;

	private final ShoppingCartService shoppingCartService;

	private final CustomerFacade customerFacade;

	public OrderApi(OrderFacade orderFacade, ShoppingCartService shoppingCartService, CustomerFacade customerFacade) {
		this.orderFacade = orderFacade;
		this.shoppingCartService = shoppingCartService;
		this.customerFacade = customerFacade;
	}

	/**
	 * Main checkout resource that will complete the order flow
	 */
	@RequestMapping(value = { "/cart/{code}/checkout" }, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableOrderConfirmation checkout(@PathVariable final String code, // shopping
																				// cart
			@Valid @RequestBody PersistableAnonymousOrder order, // order
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language) {

		Assert.notNull(order.getCustomer(), "Customer must not be null");

		ShoppingCart cart;
		try {
			cart = shoppingCartService.loadCartByCode(code, merchantStore, language);

			if (cart == null) {
				throw new ResourceNotFoundException("Cart code " + code + " does not exist");
			}

			Customer customer = new Customer();
			customer = customerFacade.populateCustomerModel(customer, order.getCustomer(), merchantStore, language);

			order.setShoppingCartId(cart.getId());

			Order modelOrder = orderFacade.processOrder(order, customer, merchantStore, language,
					LocaleUtils.getLocale(language));
			Long orderId = modelOrder.getId();
			// populate order confirmation
			order.setId(orderId);
			// set customer id
			order.getCustomer().setId(modelOrder.getCustomerId());

			return orderFacade.orderConfirmation(modelOrder, customer, merchantStore, language);

		}
		catch (Exception e) {

			String message = e.getMessage();
			if (StringUtils.isBlank(message)) { // exception type
				message = "APP-BACKEND";
				if (e.getCause() instanceof IntegrationException) {
					message = "Integration problen occured to complete order";
				}
			}
			throw new ServiceRuntimeException("Error during checkout [" + message + "]", e);
		}
	}

	@RequestMapping(value = { "/private/orders" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ConditionalOnApiStatus
	public ReadableOrderList list(@RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "id", required = false) Long id,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "phone", required = false) String phone,
			@RequestParam(value = "email", required = false) String email,
			@Parameter(hidden = true) StoreMerchantId merchantStore, @Parameter(hidden = true) LanguageCode language,
			Pageable pageable) {

		OrderCriteria orderCriteria = new OrderCriteria();
		orderCriteria.setPageable(pageable);

		orderCriteria.setCustomerName(name);
		orderCriteria.setCustomerPhone(phone);
		orderCriteria.setStatus(status);
		orderCriteria.setEmail(email);
		orderCriteria.setId(id);
		return orderFacade.getReadableOrderList(orderCriteria, merchantStore);
	}

	@RequestMapping(value = { "/private/orders/{id}" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableOrder get(@PathVariable final Long id, @Parameter(hidden = true) StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {

		return orderFacade.getReadableOrder(id, merchantStore, language);
	}

}
