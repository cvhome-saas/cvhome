package com.asrevo.cvhome.catalog.api.v1.product;

import java.util.List;

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
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.model.product.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.catalog.service.facade.product.ProductTypeFacade;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

/**
 * API to create, read, update and delete a Product API to create Manufacturer
 *
 * @author Carl Samson
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product type resource (Product Type Api)")
@Slf4j
public class ProductTypeApi {

    private final ProductTypeFacade productTypeFacade;

    public ProductTypeApi(ProductTypeFacade productTypeFacade) {
        this.productTypeFacade = productTypeFacade;
    }

    @GetMapping(value = "/private/product/types", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get product types list",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductTypeList list(StoreMerchantId merchantStore, LanguageCode language, Pageable pageable) {

        return productTypeFacade.getByMerchant(merchantStore, LanguageCode.allLanguage(), pageable);
    }

    @GetMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get product type",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductType.class))))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableProductType get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language) {

        return productTypeFacade.get(merchantStore, id, LanguageCode.allLanguage());
    }

    @GetMapping(value = "/private/product/type/unique", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Verify if product type is unique",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> exists(@RequestParam String code, StoreMerchantId merchantStore,
                                               LanguageCode language) {

        boolean exists = productTypeFacade.exists(code, merchantStore, language);
        return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
    }

    @PostMapping(value = "/private/product/type", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "POST", description = "Create product type",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Entity.class))))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public Entity create(@RequestBody PersistableProductType type, StoreMerchantId merchantStore,
                         LanguageCode language) {

        Long id = productTypeFacade.save(type, merchantStore, LanguageCode.allLanguage());
        Entity entity = new Entity();
        entity.setId(id);
        return entity;
    }

    @PutMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "PUT", description = "Update product type",
            responses = @ApiResponse(content = @Content(schema = @Schema())))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void update(@RequestBody PersistableProductType type, @PathVariable Long id, StoreMerchantId merchantStore,
                       LanguageCode language) {

        productTypeFacade.update(type, id, merchantStore, language);
    }

    @DeleteMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "DELETE", description = "Delete product type",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language) {

        productTypeFacade.delete(id, merchantStore, language);
    }

}
