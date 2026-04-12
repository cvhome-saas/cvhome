package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.group.ProductGroup;
import com.asrevo.cvhome.catalog.model.product.group.ReadableProductGroupList;
import com.asrevo.cvhome.catalog.service.facade.items.ProductItemsFacade;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Used for product grouping such as featured items
 *
 * @author carlsamson
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product groups management resource (Product Groups Management Api)")
@Slf4j
public class ProductGroupApi {

	private final ProductService productService;

	private final ProductItemsFacade productItemsFacade;

	public ProductGroupApi(ProductService productService, ProductItemsFacade productItemsFacade) {
		this.productService = productService;
		this.productItemsFacade = productItemsFacade;
	}

	@ResponseStatus(HttpStatus.OK)
	@PostMapping("/private/products/group")
	@Operation(method = "POST", description = "Create product group",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ProductGroup.class))))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ProductGroup creteGroup(@RequestBody ProductGroup group, StoreMerchantId merchantStore) {

		return productItemsFacade.createProductGroup(group, merchantStore);
	}

	@ResponseStatus(HttpStatus.OK)
	@PatchMapping("/private/products/group/{code}")
	@Operation(method = "PATCH", description = "Update product group visible flag",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ProductGroup.class))))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void updateGroup(@RequestBody ProductGroup group, @PathVariable String code, StoreMerchantId merchantStore) {

		productItemsFacade.updateProductGroup(code, group, merchantStore);
	}

	@GetMapping("/private/product/groups")
	@Operation(method = "GET", description = "Get products groups for a given merchant",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductGroupList list(StoreMerchantId merchantStore, LanguageCode language) {

		return productItemsFacade.listProductGroups(merchantStore, language);
	}

	/**
	 * Query for a product group public/product/group/{code}?lang=fr|en no lang it will
	 * take session lang or default store lang code can be any code used while creating
	 * product group, defeult being FEATURED
	 */
	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/private/products/group/{code}")
	@Operation(method = "GET", description = "Get products by group code",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductList.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductList productItemsByGroup(@PathVariable final String code, StoreMerchantId merchantStore,
			LanguageCode language) {
		return productItemsFacade.listTinyProductsGroup(code, merchantStore, LanguageCode.nonLanguage());
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping("/products/group/{code}")
	@Operation(method = "GET", description = "Get products by group code",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductList.class))))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public ReadableProductList getProductItemsByGroup(@PathVariable final String code, StoreMerchantId merchantStore,
			LanguageCode language) {
		return productItemsFacade.listMinimalProductsGroup(code, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = "/private/products/{productId}/group/{code}", method = RequestMethod.POST)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void addProductToGroup(@PathVariable Long productId, @PathVariable String code,
			StoreMerchantId merchantStore, LanguageCode language) {
		Product product = productService.findOne(productId, merchantStore);
		productItemsFacade.addItemToGroup(product, code, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = "/private/products/{productId}/group/{code}", method = RequestMethod.DELETE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void removeProductFromGroup(@PathVariable Long productId, @PathVariable String code,
			StoreMerchantId merchantStore, LanguageCode language) {

		Product product = productService.findOne(productId, merchantStore);
		productItemsFacade.removeItemFromGroup(product, code, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@DeleteMapping("/private/products/group/{code}")
	@Operation(method = "DELETE", description = "Delete product group by group code",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void deleteGroup(@PathVariable final String code, StoreMerchantId merchantStore) {

		productItemsFacade.deleteGroup(code, merchantStore);
	}

}
