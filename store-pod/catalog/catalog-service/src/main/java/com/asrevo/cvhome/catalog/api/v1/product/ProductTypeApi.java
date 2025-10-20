package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductType;
import com.asrevo.cvhome.catalog.model.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.catalog.service.facade.product.ProductTypeFacade;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.Entity;
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
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    @Operation(
            method = "GET",
            description = "Get product types list",
            responses =
                    @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductTypeList list(
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language,
            Pageable pageable) {

        return productTypeFacade.getByMerchant(merchantStore, LanguageCode.allLanguage(), pageable);
    }

    @GetMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            method = "GET",
            description = "Get product type",
            responses =
                    @ApiResponse(
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    ReadableProductType.class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductType get(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        return productTypeFacade.get(merchantStore, id, LanguageCode.allLanguage());
    }

    @GetMapping(value = "/private/product/type/unique", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            method = "GET",
            description = "Verify if product type is unique",
            responses =
                    @ApiResponse(
                            content =
                                    @Content(
                                            schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ResponseEntity<EntityExists> exists(
            @RequestParam String code,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        boolean exists = productTypeFacade.exists(code, merchantStore, language);
        return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);
    }

    @PostMapping(value = "/private/product/type", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            method = "POST",
            description = "Create product type",
            responses =
                    @ApiResponse(
                            content = @Content(schema = @Schema(implementation = Entity.class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public Entity create(
            @RequestBody PersistableProductType type,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        Long id = productTypeFacade.save(type, merchantStore, LanguageCode.allLanguage());
        Entity entity = new Entity();
        entity.setId(id);
        return entity;
    }

    @PutMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            method = "PUT",
            description = "Update product type",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(
            @RequestBody PersistableProductType type,
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        productTypeFacade.update(type, id, merchantStore, language);
    }

    @DeleteMapping(
            value = "/private/product/type/{id}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            method = "DELETE",
            description = "Delete product type",
            responses =
                    @ApiResponse(
                            content =
                                    @Content(
                                            schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1_STR)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        productTypeFacade.delete(id, merchantStore, language);
    }
}
