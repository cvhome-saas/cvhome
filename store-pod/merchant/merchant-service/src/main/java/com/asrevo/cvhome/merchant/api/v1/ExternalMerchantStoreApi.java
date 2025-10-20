package com.asrevo.cvhome.merchant.api.v1;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.api.ExternalMerchantStoreService;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Merchant and store management resource (Merchant - Store Management Api)")
@Slf4j
@AllArgsConstructor
public class ExternalMerchantStoreApi implements ExternalMerchantStoreService {

	private final StoreFacade storeFacade;

	@GetMapping(value = "/store")
	@Operation(method = "GET", description = "Get merchant store",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)), })
	@ConditionalOnApiStatus
	@Override
	public ReadableMerchantStore getStore(StoreMerchantId merchantStore) {
		return storeFacade.getReadableMerchantStoreId(merchantStore);
	}

}
