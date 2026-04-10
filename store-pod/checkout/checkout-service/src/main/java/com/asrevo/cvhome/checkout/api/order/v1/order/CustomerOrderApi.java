package com.asrevo.cvhome.checkout.api.order.v1.order;

import com.asrevo.cvhome.checkout.model.order.OrderCriteria;
import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrder;
import com.asrevo.cvhome.checkout.model.order.v0.ReadableOrderList;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "Customer Order resource", description = "Customer orders")
@Slf4j
public class CustomerOrderApi {

	private final OrderFacade orderFacade;

	private final CustomerFacade customerFacade;

	@RequestMapping(value = { "/private/customer/orders" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@ConditionalOnApiStatus
	public ReadableOrderList list(@SecuredResource StoreMerchantId merchantStore, LanguageCode language,
			Pageable pageable, JwtAuthenticationToken auth) {

		OrderCriteria orderCriteria = new OrderCriteria();
		orderCriteria.setPageable(pageable);

		String cuaExternalId = (String) auth.getTokenAttributes().get("sub");
		return customerFacade.getCustomerByCuaExternalId(cuaExternalId).map(it -> {
			orderCriteria.setCustomerId(it.getId());
			return orderCriteria;
		}).map(it -> orderFacade.getReadableOrderList(it, merchantStore)).orElseGet(ReadableOrderList::new);

	}

	@RequestMapping(value = { "/private/customer/{id}/order" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableOrder get(@PathVariable final Long id, @SecuredResource StoreMerchantId merchantStore,
			LanguageCode language, JwtAuthenticationToken auth) {
		String cuaExternalId = (String) auth.getTokenAttributes().get("sub");
		return customerFacade.getCustomerByCuaExternalId(cuaExternalId)
			.map(it -> orderFacade.getReadableOrder(id, it.getId(), merchantStore, language))
			.orElseThrow(() -> new ResourceNotFoundException("Customer not found for sub : " + cuaExternalId));
	}

	@RequestMapping(value = { "/private/customer/{id}/order/history" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public List<ReadableOrderStatusHistory> history(@PathVariable final Long id,
			@SecuredResource StoreMerchantId merchantStore, LanguageCode language, JwtAuthenticationToken auth) {

		String cuaExternalId = (String) auth.getTokenAttributes().get("sub");
		return customerFacade.getCustomerByCuaExternalId(cuaExternalId)
			.map(it -> orderFacade.getReadableOrderHistory(id, it.getId(), merchantStore, language))
			.orElseThrow(() -> new ResourceNotFoundException("Customer not found for sub : " + cuaExternalId));
	}

}
