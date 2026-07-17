package com.asrevo.cvhome.catalog.api.v1.product;

import java.util.List;

import jakarta.validation.Valid;

import org.apache.commons.lang3.StringUtils;
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

import com.asrevo.cvhome.catalog.model.product.attribute.optionset.PersistableProductOptionSet;
import com.asrevo.cvhome.catalog.model.product.attribute.optionset.ReadableProductOptionSet;
import com.asrevo.cvhome.catalog.service.facade.product.ProductOptionSetFacade;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product property set regroupment management resource (Product Options Set" + " Management Api)")
public class ProductPropertySetApi {

    private final ProductOptionSetFacade productOptionSetFacade;

    public ProductPropertySetApi(ProductOptionSetFacade productOptionSetFacade) {
        this.productOptionSetFacade = productOptionSetFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/property/set"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void create(@Valid @RequestBody PersistableProductOptionSet optionSet, StoreMerchantId merchantStore,
                       LanguageCode language) {

        productOptionSetFacade.create(optionSet, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/property/set/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @Operation(method = "GET", description = "Check if option set code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code, StoreMerchantId merchantStore,
                                               LanguageCode language) {

        boolean isOptionExist = productOptionSetFacade.exists(code, merchantStore);
        return new ResponseEntity<>(new EntityExists(isOptionExist), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/property/set/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductOptionSet get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language) {

        return productOptionSetFacade.get(id, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/property/set/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void update(@Valid @RequestBody PersistableProductOptionSet option, @PathVariable Long id,
                       StoreMerchantId merchantStore, LanguageCode language) {

        option.setId(id);
        productOptionSetFacade.update(id, option, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/property/set/{id}"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language) {

        productOptionSetFacade.delete(id, merchantStore);
    }

    /**
     * Get property set by store filter by product type
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/property/set"})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public List<ReadableProductOptionSet> list(StoreMerchantId merchantStore, LanguageCode language,
                                               @RequestParam(value = "productType", required = false) String type) {

        if (!StringUtils.isBlank(type)) {
            return productOptionSetFacade.list(merchantStore, language, type);
        } else {
            return productOptionSetFacade.list(merchantStore, language);
        }
    }

}
