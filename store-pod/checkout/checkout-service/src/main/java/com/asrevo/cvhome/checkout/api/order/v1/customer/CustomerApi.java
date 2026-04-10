package com.asrevo.cvhome.checkout.api.order.v1.customer;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.checkout.model.order.history.ReadableOrderStatusHistory;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomer;
import com.asrevo.cvhome.customer.model.customer.ReadableCustomerList;
import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.checkout.service.facade.customer.CustomerFacade;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableCustomerList list(@SecuredResource StoreMerchantId merchantStore, LanguageCode language,
			Pageable pageable) {
		CustomerCriteria customerCriteria = new CustomerCriteria();
		customerCriteria.setPageable(pageable);
		return customerFacade.getListByStore(merchantStore, customerCriteria, LanguageCode.nonLanguage());
	}

	@RequestMapping(value = { "/private/customer/info" }, method = RequestMethod.GET)
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableCustomer getCustomerInfo(@SecuredResource StoreMerchantId merchantStore, LanguageCode language,
			JwtAuthenticationToken auth) {

		String cuaExternalId = (String) auth.getTokenAttributes().get("sub");
		return customerFacade.getCustomerByCuaExternalId(cuaExternalId)
			.map(it -> customerFacade.getCustomerById(it.getId(), merchantStore, language))
			.orElseGet(ReadableCustomer::new);
	}

}
