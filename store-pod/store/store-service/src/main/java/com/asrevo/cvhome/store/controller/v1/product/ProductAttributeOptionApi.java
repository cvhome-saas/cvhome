package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.api.*;
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.service.facade.product.ProductOptionFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product attributes and options / options values management resource (Product Option Management Api)")
public class ProductAttributeOptionApi {

    @Autowired
    private ProductOptionFacade productOptionFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/option"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductOptionEntity createOption(
            @Valid @RequestBody PersistableProductOptionEntity option, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language, HttpServletRequest request, HttpServletResponse response) {

        ReadableProductOptionEntity entity = productOptionFacade.saveOption(option, merchantStore, language);
        return entity;

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/option/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Check if option code already exists", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class)))
    )
    public ResponseEntity<EntityExists> optionExists(@RequestParam(value = "code") String code,
                                                     @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        boolean isOptionExist = productOptionFacade.optionExists(code, merchantStore);
        return new ResponseEntity<EntityExists>(new EntityExists(isOptionExist), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/option/value/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Check if option value code already exists", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class)))
    )
    public ResponseEntity<EntityExists> optionValueExists(@RequestParam(value = "code") String code,
                                                          @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {
        boolean isOptionExist = productOptionFacade.optionValueExists(code, merchantStore);
        return new ResponseEntity<EntityExists>(new EntityExists(isOptionExist), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/option/value"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductOptionValue createOptionValue(
            @Valid @RequestBody PersistableProductOptionValue optionValue,
            //@RequestParam(name = "file", required = false) MultipartFile file,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request,
            HttpServletResponse response) {

        ReadableProductOptionValue entity = productOptionFacade.saveOptionValue(optionValue,
                merchantStore, language);
        return entity;

    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/option/value/{id}/image"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void addOptionValueImage(
            @PathVariable Long id,
            @RequestParam(name = "file", required = true) MultipartFile file,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request,
            HttpServletResponse response) {

        productOptionFacade.addOptionValueImage(file, id, merchantStore, language);


    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/option/value/{id}/image"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void removeOptionValueImage(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request, HttpServletResponse response) {

        productOptionFacade.removeOptionValueImage(id, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/option/{id}"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ResponseBody
    public ReadableProductOptionEntity getOption(@PathVariable Long id, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                                                 @Parameter(hidden = true) Language language, HttpServletRequest request, HttpServletResponse response) {

        return productOptionFacade.getOption(id, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/option/value/{id}"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ResponseBody
    public ReadableProductOptionValue getOptionValue(@PathVariable Long id, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                                                     @Parameter(hidden = true) Language language, HttpServletRequest request, HttpServletResponse response) {

        return productOptionFacade.getOptionValue(id, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/option/{optionId}"}, method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updateOption(@Valid @RequestBody PersistableProductOptionEntity option, @PathVariable Long optionId,
                             @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language, HttpServletRequest request,
                             HttpServletResponse response) {
        option.setId(optionId);
        productOptionFacade.saveOption(option, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/option/{optionId}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteOption(@PathVariable Long optionId, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                             @Parameter(hidden = true) Language language, HttpServletRequest request, HttpServletResponse response) {

        productOptionFacade.deleteOption(optionId, merchantStore);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/option/value/{id}"}, method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updateOptionValue(
            @PathVariable Long id,
            @Valid @RequestBody PersistableProductOptionValue optionValue,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language, HttpServletRequest request,
            HttpServletResponse response) {

        optionValue.setId(id);
        productOptionFacade.saveOptionValue(optionValue, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/option/value/{id}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteOptionValue(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language, HttpServletRequest request,
            HttpServletResponse response) {

        productOptionFacade.deleteOptionValue(id, merchantStore);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/options"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductOptionList options(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        return productOptionFacade.options(merchantStore, language, name, page, count);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/options/values"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductOptionValueList optionsValues(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        return productOptionFacade.optionValues(merchantStore, language, name, page, count);

    }

    /**
     * Product attributes
     *
     * @param id
     * @param merchantStore
     * @param language
     * @param request
     * @param response
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}/attributes"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Get product attributes", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductAttributeList.class))))
    public @ResponseBody ReadableProductAttributeList attributes(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        return productOptionFacade.getAttributesList(id, merchantStore, language, page, count);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}/attribute/{attributeId}"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Get product attributes", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    public @ResponseBody ReadableProductAttributeEntity getAttribute(
            @PathVariable Long id,
            @PathVariable Long attributeId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language, HttpServletRequest request, HttpServletResponse response) {

        ReadableProductAttributeEntity entity = productOptionFacade.getAttribute(id, attributeId, merchantStore, language);
        return entity;

    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/{id}/attribute"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody Entity createAttribute(
            @PathVariable Long id,
            @Valid @RequestBody PersistableProductAttribute attribute,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request,
            HttpServletResponse response) {

        ReadableProductAttributeEntity attributeEntity = productOptionFacade.saveAttribute(id, attribute, merchantStore, language);

        Entity entity = new Entity();
        entity.setId(attributeEntity.getId());
        return entity;


    }

    /**
     * Create multiple attributes
     *
     * @param id
     * @param attributeId
     * @param merchantStore
     * @param language
     * @param request
     * @param response
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/{id}/attributes"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "POST", description = "Saves multiple attributes", summary = "application/json",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = CodeEntity.class))))
    public List<CodeEntity> createAttributes(
            @PathVariable Long id,
            @Valid @RequestBody List<PersistableProductAttribute> attributes,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {


        return productOptionFacade.createAttributes(attributes, id, merchantStore);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}/attribute/{attributeId}"}, method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updateAttribute(@PathVariable Long id, @Valid @RequestBody PersistableProductAttribute attribute, @PathVariable Long attributeId,
                                @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language, HttpServletRequest request,
                                HttpServletResponse response) {

        attribute.setId(attributeId);
        productOptionFacade.saveAttribute(id, attribute, merchantStore, language);

    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}/attribute/{attributeId}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteAttribute(@PathVariable Long id, @PathVariable Long attributeId, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                                @Parameter(hidden = true) Language language, HttpServletRequest request, HttpServletResponse response) {

        productOptionFacade.deleteAttribute(id, attributeId, merchantStore);

    }

}