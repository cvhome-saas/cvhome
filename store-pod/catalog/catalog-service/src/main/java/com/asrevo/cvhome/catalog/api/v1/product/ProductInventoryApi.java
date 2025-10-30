package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.inventory.PersistableInventory;
import com.asrevo.cvhome.catalog.model.product.inventory.ReadableInventory;
import com.asrevo.cvhome.catalog.service.facade.product.ProductInventoryFacade;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.RestApiException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product inventory resource (Product Inventory Api)")
@Slf4j
public class ProductInventoryApi {

	private final ProductInventoryFacade productInventoryFacade;

	public ProductInventoryApi(ProductInventoryFacade productInventoryFacade) {
		this.productInventoryFacade = productInventoryFacade;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { "/private/product/{productId}/inventory" }, method = RequestMethod.POST)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public @ResponseBody ReadableInventory create(@PathVariable Long productId,
			@Valid @RequestBody PersistableInventory inventory,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {
		inventory.setProductId(productId);
		return productInventoryFacade.add(inventory, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{productId}/inventory/{id}" }, method = RequestMethod.PUT)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public void update(@PathVariable Long productId, @PathVariable Long id,
			@Valid @RequestBody PersistableInventory inventory,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {
		inventory.setId(id);
		inventory.setProductId(inventory.getProductId());
		inventory.setVariant(inventory.getVariant());
		inventory.setProductId(productId);
		productInventoryFacade.update(inventory, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{productId}/inventory/{id}" }, method = RequestMethod.DELETE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public void delete(@PathVariable Long productId, @PathVariable Long id,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {

		productInventoryFacade.delete(productId, id, merchantStore);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/product/{sku}/inventory" })
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public @ResponseBody ReadableEntityList<ReadableInventory> getBySku(@PathVariable String sku,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, Pageable pageable) {

		return productInventoryFacade.get(sku, merchantStore, language, pageable);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/product/inventory" })
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public @ResponseBody ReadableEntityList<ReadableInventory> getByProductId(@RequestParam Long productId,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, Pageable pageable) {

		if (productId == null) {
			throw new RestApiException("Requires request parameter product id [/product/inventoty?productId");
		}

		return productInventoryFacade.get(productId, merchantStore, language, pageable);
	}

}
