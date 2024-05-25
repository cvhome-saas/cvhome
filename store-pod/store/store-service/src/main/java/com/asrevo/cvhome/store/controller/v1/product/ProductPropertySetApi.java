package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.service.facade.product.ProductOptionSetFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product property set regroupment management resource (Product Options Set Management Api)")
public class ProductPropertySetApi {

    private final ProductOptionSetFacade productOptionSetFacade;

    public ProductPropertySetApi(ProductOptionSetFacade productOptionSetFacade) {
        this.productOptionSetFacade = productOptionSetFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/property/set"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void create(
            @Valid @RequestBody PersistableProductOptionSet optionSet,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        productOptionSetFacade.create(optionSet, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/property/set/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Check if option set code already exists", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    public ResponseEntity<EntityExists> exists(
            @RequestParam(value = "code") String code,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        boolean isOptionExist = productOptionSetFacade.exists(code, merchantStore);
        return new ResponseEntity<EntityExists>(new EntityExists(isOptionExist), HttpStatus.OK);
    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/property/set/{id}"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ResponseBody
    public ReadableProductOptionSet get(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        return productOptionSetFacade.get(id, merchantStore, language);

    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/property/set/{id}"}, method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(
            @Valid @RequestBody PersistableProductOptionSet option,
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        option.setId(id);
        productOptionSetFacade.update(id, option, merchantStore, language);

    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/property/set/{id}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        productOptionSetFacade.delete(id, merchantStore);

    }

    /**
     * Get property set by store
     * filter by product type
     *
     * @param merchantStore
     * @param language
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/property/set"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody List<ReadableProductOptionSet> list(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "productType", required = false) String type) {

        if (!StringUtils.isBlank(type)) {
            return productOptionSetFacade.list(merchantStore, language, type);
        } else {
            return productOptionSetFacade.list(merchantStore, language);
        }


    }


}