package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupList;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupListV2;
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
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product Groups Management Resource V2")
@Slf4j
public class ProductGroupV2Api {

	private final ProductGroupFacade productGroupFacade;

	public ProductGroupV2Api(ProductGroupFacade productGroupFacade) {
		this.productGroupFacade = productGroupFacade;
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/products/groups/{code}")
	@Operation(summary = "Get product group by code",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductGroup.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public ReadableProductGroup getProductGroup(@PathVariable String code, StoreMerchantId merchantStore,
			LanguageCode language) {
		return productGroupFacade.getByCode(merchantStore, code, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/private/products/groups")
	@Operation(summary = "List product groups for a store",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableProductGroupListV2.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductGroupListV2 listProductGroups(StoreMerchantId merchantStore, LanguageCode language,
			Pageable pageable) {
		return productGroupFacade.list(merchantStore, language, pageable);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/private/products/groups")
	@Operation(summary = "Create or update a product group")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public PersistableProductGroup saveProductGroup(@RequestBody @Valid PersistableProductGroup group,
			StoreMerchantId merchantStore) {
		return productGroupFacade.saveProductGroup(merchantStore, group);
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/private/products/groups/{code}")
	@Operation(summary = "Delete a product group")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void deleteProductGroup(@PathVariable String code, StoreMerchantId merchantStore) {
		productGroupFacade.delete(merchantStore, code);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping("/private/products/groups/{code}/product/{productId}")
	@Operation(summary = "Add a product to a group")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void addProductToGroup(@PathVariable String code, @PathVariable Long productId,
			StoreMerchantId merchantStore) {
		productGroupFacade.addProductToGroup(merchantStore, code, productId);
	}

	@ResponseStatus(HttpStatus.NO_CONTENT)
	@DeleteMapping("/private/products/groups/{code}/product/{productId}")
	@Operation(summary = "Remove a product from a group")
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void removeProductFromGroup(@PathVariable String code, @PathVariable Long productId,
			StoreMerchantId merchantStore) {
		productGroupFacade.removeProductFromGroup(merchantStore, code, productId);
	}

}
