package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.service.facade.product.group.ProductGroupFacade;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product Relationship V2 Management Api")
@Slf4j
public class ProductRelationshipApi {

	private static final String DEFAULT_RELATIONSHIP_CODE = "RELATED_ITEM";

	private final ProductGroupFacade productGroupFacade;

	public ProductRelationshipApi(ProductGroupFacade productGroupFacade) {
		this.productGroupFacade = productGroupFacade;
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/products/{id}/relationship")
	@Operation(summary = "Get default relationship group for a product (V2)",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductGroup.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public ReadableProductGroup getProductRelationships(@PathVariable Long id, StoreMerchantId merchantStore,
			LanguageCode language) {
		return productGroupFacade.getByCodeAndParent(merchantStore, id, DEFAULT_RELATIONSHIP_CODE, language);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/private/products/{id}/relationship/{productId}")
	@Operation(summary = "Add a product to the default relationship group of a parent product (V2)")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void addProductToRelationship(@PathVariable Long id, @PathVariable Long productId,
			StoreMerchantId merchantStore) {
		productGroupFacade.addProductToGroupForParent(merchantStore, id, DEFAULT_RELATIONSHIP_CODE, productId);
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/private/products/{id}/relationship/{productId}")
	@Operation(summary = "Remove a product from the default relationship group of a parent product (V2)")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void removeProductFromRelationship(@PathVariable Long id, @PathVariable Long productId,
			StoreMerchantId merchantStore) {
		productGroupFacade.removeProductFromGroupForParent(merchantStore, id, DEFAULT_RELATIONSHIP_CODE, productId);
	}

}
