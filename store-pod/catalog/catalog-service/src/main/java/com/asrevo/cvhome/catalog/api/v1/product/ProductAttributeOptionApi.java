package com.asrevo.cvhome.catalog.api.v1.product;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductAttribute;
import com.asrevo.cvhome.catalog.model.product.attribute.PersistableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.PersistableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductAttributeList;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionEntity;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionList;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValue;
import com.asrevo.cvhome.catalog.model.product.attribute.api.ReadableProductOptionValueList;
import com.asrevo.cvhome.catalog.service.facade.product.ProductOptionFacade;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.CodeEntity;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.commons.domain.LanguageCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product attributes and options / options values management resource (Product" + " Option Management Api)")
public class ProductAttributeOptionApi {

    private final ProductOptionFacade productOptionFacade;

    public ProductAttributeOptionApi(ProductOptionFacade productOptionFacade) {
        this.productOptionFacade = productOptionFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/option"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductOptionEntity createOption(@Valid @RequestBody PersistableProductOptionEntity option,
                                                    StoreMerchantId merchantStore, LanguageCode language) {

        return productOptionFacade.saveOption(option, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/option/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Check if option code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> optionExists(@RequestParam(value = "code") String code,
                                                     StoreMerchantId merchantStore, LanguageCode language) {

        boolean isOptionExist = productOptionFacade.optionExists(code, merchantStore);
        return new ResponseEntity<>(new EntityExists(isOptionExist), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/option/value/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Check if option value code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> optionValueExists(@RequestParam(value = "code") String code,
                                                          StoreMerchantId merchantStore, LanguageCode language) {
        boolean isOptionExist = productOptionFacade.optionValueExists(code, merchantStore);
        return new ResponseEntity<>(new EntityExists(isOptionExist), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/option/value"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductOptionValue createOptionValue(@Valid @RequestBody PersistableProductOptionValue optionValue,
                                                        // @RequestParam(name = "file", required = false) MultipartFile file,
                                                        StoreMerchantId merchantStore, LanguageCode language) {

        return productOptionFacade.saveOptionValue(optionValue, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/option/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")

    public ReadableProductOptionEntity getOption(@PathVariable Long id, StoreMerchantId merchantStore,
                                                 LanguageCode language) {

        return productOptionFacade.getOption(id, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/option/value/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")

    public ReadableProductOptionValue getOptionValue(@PathVariable Long id, StoreMerchantId merchantStore,
                                                     LanguageCode language) {

        return productOptionFacade.getOptionValue(id, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/option/{optionId}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void updateOption(@Valid @RequestBody PersistableProductOptionEntity option, @PathVariable Long optionId,
                             StoreMerchantId merchantStore, LanguageCode language) {
        option.setId(optionId);
        productOptionFacade.saveOption(option, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/option/{optionId}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void deleteOption(@PathVariable Long optionId, StoreMerchantId merchantStore) {

        productOptionFacade.deleteOption(optionId, merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/option/value/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void updateOptionValue(@PathVariable Long id, @Valid @RequestBody PersistableProductOptionValue optionValue,
                                  StoreMerchantId merchantStore, LanguageCode language) {

        optionValue.setId(id);
        productOptionFacade.saveOptionValue(optionValue, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/option/value/{id}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void deleteOptionValue(@PathVariable Long id, StoreMerchantId merchantStore) {

        productOptionFacade.deleteOptionValue(id, merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/options"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductOptionList options(StoreMerchantId merchantStore, LanguageCode language,
                                             @RequestParam(value = "name", required = false) String name, Pageable pageable) {

        return productOptionFacade.options(merchantStore, language, name, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/options/values"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductOptionValueList optionsValues(StoreMerchantId merchantStore, LanguageCode language,
                                                        @RequestParam(value = "name", required = false) String name, Pageable pageable) {

        return productOptionFacade.optionValues(merchantStore, language, name, pageable);
    }

    /**
     * Product attributes
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{id}/attributes"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Get product attributes",
            responses = @ApiResponse(
                    content = @Content(schema = @Schema(implementation = ReadableProductAttributeList.class))))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductAttributeList attributes(@PathVariable Long id, StoreMerchantId merchantStore,
                                                   LanguageCode language, Pageable pageable) {

        return productOptionFacade.getAttributesList(id, merchantStore, language, pageable);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{id}/attribute/{attributeId}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Get product attributes",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductAttributeEntity getAttribute(@PathVariable Long id, @PathVariable Long attributeId,
                                                       StoreMerchantId merchantStore, LanguageCode language) {

        return productOptionFacade.getAttribute(id, attributeId, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/{id}/attribute"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
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
    @PostMapping(value = {"/private/product/{id}/attributes"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "POST", description = "Saves multiple attributes", summary = "application/json",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = CodeEntity.class))))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public List<CodeEntity> createAttributes(@PathVariable Long id,
                                             @Valid @RequestBody List<PersistableProductAttribute> attributes,
                                             StoreMerchantId merchantStore,
                                             LanguageCode language) {

        return productOptionFacade.createAttributes(attributes, id, merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/{id}/attribute/{attributeId}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void updateAttribute(@PathVariable Long id, @Valid @RequestBody PersistableProductAttribute attribute,
                                @PathVariable Long attributeId, StoreMerchantId merchantStore, LanguageCode language) {

        attribute.setId(attributeId);
        productOptionFacade.saveAttribute(id, attribute, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/{id}/attribute/{attributeId}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void deleteAttribute(@PathVariable Long id, @PathVariable Long attributeId, StoreMerchantId merchantStore) {

        productOptionFacade.deleteAttribute(id, attributeId, merchantStore);
    }

}
