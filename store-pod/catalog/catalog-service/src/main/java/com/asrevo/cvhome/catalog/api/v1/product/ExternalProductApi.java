package com.asrevo.cvhome.catalog.api.v1.product;

import com.asrevo.cvhome.catalog.model.product.ProductDetails;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.services.product.ExternalProductService;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product definition resource (Create update and delete product definition. Serves"
		+ " api v1 and v2 with backward compatibility)")
@Slf4j
@AllArgsConstructor
public class ExternalProductApi implements ExternalProductService {

	private final ProductService productService;

	@GetMapping(value = "/detailed-product")
	@Operation(method = "GET", description = "Get Full Product Details",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProduct.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "sku", schema = @Schema(name = "sku", type = "string")),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	@Override
	public ProductDetails getDetailedProduct(StoreMerchantId store, @RequestParam String sku, LanguageCode lang) {
		return productService.getDetailedProduct(store, sku, lang);
	}

}
