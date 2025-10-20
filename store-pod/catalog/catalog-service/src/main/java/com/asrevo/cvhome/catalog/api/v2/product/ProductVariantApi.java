package com.asrevo.cvhome.catalog.api.v2.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.product.variant.PersistableProductVariant;
import com.asrevo.cvhome.catalog.model.product.product.variant.ReadableProductVariant;
import com.asrevo.cvhome.catalog.service.facade.product.ProductVariantFacade;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * Api to manage productVariant
 * <p>
 * Product variant allows to specify product size, sku and options related to this product
 * variant
 *
 * @author carlsamson
 */
@Controller
@RequestMapping("/api/v2")
@Tags(value = @Tag(name = "Product variants api"))
@Slf4j
public class ProductVariantApi {

	private final ProductVariantFacade productVariantFacade;

	public ProductVariantApi(ProductVariantFacade productVariantFacade) {
		this.productVariantFacade = productVariantFacade;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value = { "/private/product/{productId}/variant" })
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public @ResponseBody Entity create(@Valid @RequestBody PersistableProductVariant variant,
			@PathVariable Long productId, @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {
		Long id = productVariantFacade.create(variant, productId, merchantStore, language);
		return new Entity(id);
	}

	@ResponseStatus(HttpStatus.OK)
	@PutMapping(value = { "/private/product/{id}/variant/{variantId}" })
	@Operation(method = "PUT", description = "Update product variant",
			responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema())))
	public @ResponseBody void update(@PathVariable Long id, @PathVariable Long variantId,
			@Valid @RequestBody PersistableProductVariant variant,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {
		productVariantFacade.update(variantId, variant, id, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/product/{id}/variant/{sku}/unique" }, produces = "application/json")
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "Check if option set code already exists",
			responses = { @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))) })
	public @ResponseBody ResponseEntity<EntityExists> exists(@PathVariable Long id, @PathVariable String sku,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {

		boolean exist = productVariantFacade.exists(sku, merchantStore, id, language);
		return new ResponseEntity<>(new EntityExists(exist), HttpStatus.OK);
	}

	@GetMapping(value = "/private/product/{id}/variant/{variantId}", produces = "application/json")
	@Operation(method = "GET", description = "Get a productVariant by id",
			summary = "For administration and shop purpose. Specifying ?merchant is required"
					+ " otherwise it falls back to DEFAULT")
	@ApiResponse(responseCode = "200", description = "Single product found",
			content = @Content(schema = @Schema(implementation = ReadableProductVariant.class)))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public @ResponseBody ReadableProductVariant get(@PathVariable final Long id, @PathVariable Long variantId,
			@RequestParam(value = "lang", required = false) String lang,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {

		return productVariantFacade.get(variantId, id, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{id}/variants" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public @ResponseBody ReadableEntityList<ReadableProductVariant> list(@PathVariable final Long id,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language, Pageable pageable) {

		return productVariantFacade.list(id, merchantStore, language, pageable);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{id}/variant/{variantId}" }, method = RequestMethod.DELETE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	public void delete(@PathVariable Long id, @PathVariable Long variantId,
			@Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
			@Parameter(hidden = true) LanguageCode language) {

		productVariantFacade.delete(variantId, id, merchantStore);
	}

}
