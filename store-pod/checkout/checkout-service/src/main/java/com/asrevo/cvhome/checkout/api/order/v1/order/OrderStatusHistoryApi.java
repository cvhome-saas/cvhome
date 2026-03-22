package com.asrevo.cvhome.checkout.api.order.v1.order;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.checkout.model.order.history.PersistableOrderStatusHistory;
import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.checkout.service.facade.order.OrderFacade;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
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

	@Autowired
	private OrderFacade orderFacade;

	@RequestMapping(value = { "private/orders/{id}/history" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public List<ReadableOrderStatusHistory> list(@PathVariable final Long id,
			@SecuredResource StoreMerchantId merchantStore, LanguageCode language) {

		return orderFacade.getReadableOrderHistory(id, merchantStore, language);
	}

	@RequestMapping(value = { "private/orders/{id}/history" }, method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	@Operation(method = "POST", description = "Add order history", summary = "Adds a new status to an order")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@ConditionalOnApiStatus
	public void create(@PathVariable final Long id, @RequestBody PersistableOrderStatusHistory history,
			@SecuredResource StoreMerchantId merchantStore) {

		// TODO validate date format

		orderFacade.createOrderStatus(history, id, merchantStore);
	}

}
