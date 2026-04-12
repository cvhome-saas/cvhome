package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.*;
import com.asrevo.cvhome.catalog.service.facade.product.ProductOptionFacade;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product attributes and options / options values management resource (Product" + " Option Management Api)")
public class ProductAttributeOptionApi {

	private final ProductOptionFacade productOptionFacade;

	public ProductAttributeOptionApi(ProductOptionFacade productOptionFacade) {
		this.productOptionFacade = productOptionFacade;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { "/private/product/option" }, method = RequestMethod.POST)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductOptionEntity createOption(@Valid @RequestBody PersistableProductOptionEntity option,
			StoreMerchantId merchantStore, LanguageCode language) {

		return productOptionFacade.saveOption(option, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/product/option/unique" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "Check if option code already exists",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ResponseEntity<EntityExists> optionExists(@RequestParam(value = "code") String code,
			StoreMerchantId merchantStore, LanguageCode language) {

		boolean isOptionExist = productOptionFacade.optionExists(code, merchantStore);
		return new ResponseEntity<>(new EntityExists(isOptionExist), HttpStatus.OK);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/product/option/value/unique" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "Check if option value code already exists",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ResponseEntity<EntityExists> optionValueExists(@RequestParam(value = "code") String code,
			StoreMerchantId merchantStore, LanguageCode language) {
		boolean isOptionExist = productOptionFacade.optionValueExists(code, merchantStore);
		return new ResponseEntity<>(new EntityExists(isOptionExist), HttpStatus.OK);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { "/private/product/option/value" }, method = RequestMethod.POST)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductOptionValue createOptionValue(@Valid @RequestBody PersistableProductOptionValue optionValue,
			// @RequestParam(name = "file", required = false) MultipartFile file,
			StoreMerchantId merchantStore, LanguageCode language) {

		return productOptionFacade.saveOptionValue(optionValue, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/option/{id}" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")

	public ReadableProductOptionEntity getOption(@PathVariable Long id, StoreMerchantId merchantStore,
			LanguageCode language) {

		return productOptionFacade.getOption(id, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/option/value/{id}" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")

	public ReadableProductOptionValue getOptionValue(@PathVariable Long id, StoreMerchantId merchantStore,
			LanguageCode language) {

		return productOptionFacade.getOptionValue(id, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/option/{optionId}" }, method = RequestMethod.PUT)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void updateOption(@Valid @RequestBody PersistableProductOptionEntity option, @PathVariable Long optionId,
			StoreMerchantId merchantStore, LanguageCode language) {
		option.setId(optionId);
		productOptionFacade.saveOption(option, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/option/{optionId}" }, method = RequestMethod.DELETE)
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void deleteOption(@PathVariable Long optionId, StoreMerchantId merchantStore) {

		productOptionFacade.deleteOption(optionId, merchantStore);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/option/value/{id}" }, method = RequestMethod.PUT)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void updateOptionValue(@PathVariable Long id, @Valid @RequestBody PersistableProductOptionValue optionValue,
			StoreMerchantId merchantStore, LanguageCode language) {

		optionValue.setId(id);
		productOptionFacade.saveOptionValue(optionValue, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/option/value/{id}" }, method = RequestMethod.DELETE)
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void deleteOptionValue(@PathVariable Long id, StoreMerchantId merchantStore) {

		productOptionFacade.deleteOptionValue(id, merchantStore);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/options" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductOptionList options(StoreMerchantId merchantStore, LanguageCode language,
			@RequestParam(value = "name", required = false) String name, Pageable pageable) {

		return productOptionFacade.options(merchantStore, language, name, pageable);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/options/values" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductOptionValueList optionsValues(StoreMerchantId merchantStore, LanguageCode language,
			@RequestParam(value = "name", required = false) String name, Pageable pageable) {

		return productOptionFacade.optionValues(merchantStore, language, name, pageable);
	}

	/**
	 * Product attributes
	 */
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{id}/attributes" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "Get product attributes",
			responses = @ApiResponse(
					content = @Content(schema = @Schema(implementation = ReadableProductAttributeList.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductAttributeList attributes(@PathVariable Long id, StoreMerchantId merchantStore,
			LanguageCode language, Pageable pageable) {

		return productOptionFacade.getAttributesList(id, merchantStore, language, pageable);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{id}/attribute/{attributeId}" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "Get product attributes",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductAttributeEntity getAttribute(@PathVariable Long id, @PathVariable Long attributeId,
			StoreMerchantId merchantStore, LanguageCode language) {

		return productOptionFacade.getAttribute(id, attributeId, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { "/private/product/{id}/attribute" }, method = RequestMethod.POST)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public Entity createAttribute(@PathVariable Long id, @Valid @RequestBody PersistableProductAttribute attribute,
			StoreMerchantId merchantStore, LanguageCode language) {

		ReadableProductAttributeEntity attributeEntity = productOptionFacade.saveAttribute(id, attribute, merchantStore,
				language);

		Entity entity = new Entity();
		entity.setId(attributeEntity.getId());
		return entity;
	}

	/**
	 * Create multiple attributes
	 */
	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { "/private/product/{id}/attributes" }, method = RequestMethod.POST)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "POST", description = "Saves multiple attributes", summary = "application/json",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = CodeEntity.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public List<CodeEntity> createAttributes(@PathVariable Long id,
			@Valid @RequestBody List<PersistableProductAttribute> attributes, StoreMerchantId merchantStore,
			LanguageCode language) {

		return productOptionFacade.createAttributes(attributes, id, merchantStore);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{id}/attribute/{attributeId}" }, method = RequestMethod.PUT)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void updateAttribute(@PathVariable Long id, @Valid @RequestBody PersistableProductAttribute attribute,
			@PathVariable Long attributeId, StoreMerchantId merchantStore, LanguageCode language) {

		attribute.setId(attributeId);
		productOptionFacade.saveAttribute(id, attribute, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/{id}/attribute/{attributeId}" }, method = RequestMethod.DELETE)
	@Parameters({ @Parameter(name = "store",
			schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void deleteAttribute(@PathVariable Long id, @PathVariable Long attributeId, StoreMerchantId merchantStore) {

		productOptionFacade.deleteAttribute(id, attributeId, merchantStore);
	}

}
