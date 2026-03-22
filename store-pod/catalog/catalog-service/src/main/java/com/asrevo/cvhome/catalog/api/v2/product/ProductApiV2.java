package com.asrevo.cvhome.catalog.api.v2.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductList;
import com.asrevo.cvhome.catalog.model.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.service.facade.product.ProductCommonFacade;
import com.asrevo.cvhome.catalog.service.facade.product.ProductDefinitionFacade;
import com.asrevo.cvhome.catalog.service.facade.product.ProductFacade;
import com.asrevo.cvhome.commons.annotation.ApiUsage;
import com.asrevo.cvhome.commons.annotation.ConditionalOnApiStatus;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.Entity;
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
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

/**
 * API to create, read, update and delete a Product API.
 *
 * @author Carl Samson
 */
@Controller
@RequestMapping("/api/v2")
@Tags(value = @Tag(name = "Product display and management resource (Product display and"
		+ " Management Api such as adding a product to category. Serves"
		+ " api v1 and v2 with backward compatibility)"))
@Slf4j
public class ProductApiV2 {

	private final ProductDefinitionFacade productDefinitionFacade;

	private final ProductFacade productFacadeV2;

	private final ProductCommonFacade productCommonFacade;

	public ProductApiV2(ProductDefinitionFacade productDefinitionFacade, ProductFacade productFacadeV2,
			ProductCommonFacade productCommonFacade) {
		this.productDefinitionFacade = productDefinitionFacade;
		this.productFacadeV2 = productFacadeV2;
		this.productCommonFacade = productCommonFacade;
	}

	/**
	 * ------------ V2
	 * <p>
	 * --- product definition
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@PostMapping(value = { "/private/product" })
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ResponseBody
	@ConditionalOnApiStatus
	public Entity createV2(@Valid @RequestBody PersistableProductDefinition product,
			@SecuredResource StoreMerchantId merchantStore, LanguageCode language) {

		// make sure product id is null
		product.setId(null);
		Long id = productDefinitionFacade.saveProductDefinition(merchantStore, product, language);
		Entity returnEntity = new Entity();
		returnEntity.setId(id);
		return returnEntity;
	}

	@ResponseStatus(HttpStatus.OK)
	@PutMapping(value = { "/private/product/{id}" })
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public void updateV2(@PathVariable Long id, @Valid @RequestBody PersistableProductDefinition product,
			@SecuredResource StoreMerchantId merchantStore, LanguageCode language) {

		productDefinitionFacade.update(id, product, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/product/{id}" })
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ResponseBody
	@ConditionalOnApiStatus
	public ReadableProductDefinition getV2(@PathVariable Long id, @SecuredResource StoreMerchantId merchantStore,
			LanguageCode language) {

		return productDefinitionFacade.getProduct(merchantStore, id, LanguageCode.allLanguage());
	}

	/**
	 * API for getting a product
	 * @param lang ?lang=fr|en
	 * @return ReadableProduct
	 * @throws Exception
	 * <p>
	 * /api/product/123
	 */
	@RequestMapping(value = { "/product/name/{friendlyUrl}", "/product/friendly/{friendlyUrl}" },
			method = RequestMethod.GET)
	@Operation(method = "GET", description = "Get a product by friendlyUrl (slug) version 2",
			summary = "For shop purpose. Specifying ?merchant is " + "required otherwise it falls back to DEFAULT")
	@ApiResponse(responseCode = "200", description = "Single product found",
			content = @Content(schema = @Schema(implementation = ReadableProduct.class)))
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus(usage = ApiUsage.NOT_USED)
	public ReadableProduct getByfriendlyUrl(@PathVariable final String friendlyUrl,
			@RequestParam(value = "lang", required = false) String lang, StoreMerchantId merchantStore,
			LanguageCode language, HttpServletResponse response) throws Exception {

		ReadableProduct product = productFacadeV2.getProductBySeUrl(merchantStore, friendlyUrl, language);

		if (product == null) {
			response.sendError(404, "Product not fount for id " + friendlyUrl);
			return null;
		}

		return product;
	}

	@RequestMapping(value = "/private/tiny-products", method = RequestMethod.GET)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableProductList tiny(ProductCriteria searchCriteria, @SecuredResource StoreMerchantId merchantStore,
			LanguageCode language, Pageable pageable) {

		searchCriteria.setPageable(pageable);
		searchCriteria.setLanguage(language);

		return productFacadeV2.getBaseProductListsByCriteria(merchantStore, searchCriteria);
	}

	@RequestMapping(value = "/private/base-products", method = RequestMethod.GET)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableProductList base(ProductCriteria searchCriteria, @SecuredResource StoreMerchantId merchantStore,
			LanguageCode language, Pageable pageable) {

		searchCriteria.setPageable(pageable);
		searchCriteria.setLanguage(language);

		return productFacadeV2.getBaseProductListsByCriteria(merchantStore, searchCriteria);
	}

	@RequestMapping(value = "/products", method = RequestMethod.GET)
	@ResponseBody
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	public ReadableProductList getList(ProductCriteria searchCriteria, StoreMerchantId merchantStore,
			LanguageCode language, Pageable pageable) {

		searchCriteria.setPageable(pageable);
		searchCriteria.setLanguage(language);

		return productFacadeV2.getProductListsByCriteria(merchantStore, searchCriteria);
	}

	/**
	 * updates price quantity
	 **/
	@ResponseStatus(HttpStatus.OK)
	@PatchMapping(value = "/private/product/{sku}", produces = { APPLICATION_JSON_VALUE })
	@Operation(method = "PATCH", description = "Update product inventory", summary = "Updates product inventory",
			responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema())))
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@ConditionalOnApiStatus
	// @TODO check if sku is id or string
	public void update(@PathVariable String sku, @Valid @RequestBody LightPersistableProduct product,
			@SecuredResource StoreMerchantId merchantStore, LanguageCode language) {
		productCommonFacade.update(sku, product, merchantStore, language);
	}

}
