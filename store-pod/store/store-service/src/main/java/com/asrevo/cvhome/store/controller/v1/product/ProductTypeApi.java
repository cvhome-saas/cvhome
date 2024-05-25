package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.type.PersistableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.service.facade.product.ProductTypeFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

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
    @Operation(method = "GET", description = "Get product types list", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductTypeList list(@RequestParam(name = "count", defaultValue = "10") int count,
                                        @RequestParam(name = "page", defaultValue = "0") int page, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                                        @Parameter(hidden = true) Language language) {

        return productTypeFacade.getByMerchant(merchantStore, language, count, page);

    }

    @GetMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get product type",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductType.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductType get(@PathVariable Long id, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                                   @Parameter(hidden = true) Language language) {

        return productTypeFacade.get(merchantStore, id, language);

    }

    @GetMapping(value = "/private/product/type/unique", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Verify if product type is unique",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ResponseEntity<EntityExists> exists(@RequestParam String code, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                                               @Parameter(hidden = true) Language language) {

        boolean exists = productTypeFacade.exists(code, merchantStore, language);
        return new ResponseEntity<>(new EntityExists(exists), HttpStatus.OK);

    }

    @PostMapping(value = "/private/product/type", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "POST", description = "Create product type", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Entity.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public Entity create(@RequestBody PersistableProductType type, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                         @Parameter(hidden = true) Language language) {

        Long id = productTypeFacade.save(type, merchantStore, language);
        Entity entity = new Entity();
        entity.setId(id);
        return entity;

    }

    @PutMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "PUT", description = "Update product type",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(@RequestBody PersistableProductType type, @PathVariable Long id,
                       @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        productTypeFacade.update(type, id, merchantStore, language);

    }

    @DeleteMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "DELETE", description = "Delete product type", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(@PathVariable Long id, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        productTypeFacade.delete(id, merchantStore, language);

    }

}
