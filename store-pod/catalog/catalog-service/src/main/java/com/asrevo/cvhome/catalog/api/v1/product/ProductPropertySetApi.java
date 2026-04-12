package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.catalog.service.facade.product.ProductOptionSetFacade;

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
import jakarta.validation.Valid;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product property set regroupment management resource (Product Options Set" + " Management Api)")
public class ProductPropertySetApi {

	private final ProductOptionSetFacade productOptionSetFacade;

	public ProductPropertySetApi(ProductOptionSetFacade productOptionSetFacade) {
		this.productOptionSetFacade = productOptionSetFacade;
	}

	@ResponseStatus(HttpStatus.CREATED)
	@RequestMapping(value = { "/private/product/property/set" }, method = RequestMethod.POST)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void create(@Valid @RequestBody PersistableProductOptionSet optionSet, StoreMerchantId merchantStore,
			LanguageCode language) {

		productOptionSetFacade.create(optionSet, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@GetMapping(value = { "/private/product/property/set/unique" }, produces = MediaType.APPLICATION_JSON_VALUE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@Operation(method = "GET", description = "Check if option set code already exists",
			responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code, StoreMerchantId merchantStore,
			LanguageCode language) {

		boolean isOptionExist = productOptionSetFacade.exists(code, merchantStore);
		return new ResponseEntity<>(new EntityExists(isOptionExist), HttpStatus.OK);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/property/set/{id}" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })

	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public ReadableProductOptionSet get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language) {

		return productOptionSetFacade.get(id, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/property/set/{id}" }, method = RequestMethod.PUT)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void update(@Valid @RequestBody PersistableProductOptionSet option, @PathVariable Long id,
			StoreMerchantId merchantStore, LanguageCode language) {

		option.setId(id);
		productOptionSetFacade.update(id, option, merchantStore, language);
	}

	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/property/set/{id}" }, method = RequestMethod.DELETE)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public void delete(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language) {

		productOptionSetFacade.delete(id, merchantStore);
	}

	/**
	 * Get property set by store filter by product type
	 */
	@ResponseStatus(HttpStatus.OK)
	@RequestMapping(value = { "/private/product/property/set" }, method = RequestMethod.GET)
	@Parameters({
			@Parameter(name = "store",
					schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
			@Parameter(name = "lang",
					schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE)) })
	@PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
	public List<ReadableProductOptionSet> list(StoreMerchantId merchantStore, LanguageCode language,
			@RequestParam(value = "productType", required = false) String type) {

		if (!StringUtils.isBlank(type)) {
			return productOptionSetFacade.list(merchantStore, language, type);
		}
		else {
			return productOptionSetFacade.list(merchantStore, language);
		}
	}

}
