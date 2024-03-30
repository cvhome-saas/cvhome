package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.type.PersistableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductType;
import com.asrevo.cvhome.store.core.model.catalog.product.type.ReadableProductTypeList;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.service.facade.product.ProductTypeFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API to create, read, update and delete a Product API to create Manufacturer
 *
 * @author Carl Samson
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product type resource (Product Type Api)")
public class ProductTypeApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductTypeApi.class);
    @Autowired
    private ProductTypeFacade productTypeFacade;

    @GetMapping(value = "/private/product/types", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get product types list", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = "DEFAULT")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = "en"))
    })
    public ReadableProductTypeList list(@RequestParam(name = "count", defaultValue = "10") int count,
                                        @RequestParam(name = "page", defaultValue = "0") int page, @Parameter(hidden = true) MerchantStore merchantStore,
                                        @Parameter(hidden = true) Language language) {

        return productTypeFacade.getByMerchant(merchantStore, language, count, page);

    }

    @GetMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get product type", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableProductType.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = "DEFAULT")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = "en"))
    })
    public ReadableProductType get(@PathVariable Long id, @Parameter(hidden = true) MerchantStore merchantStore,
                                   @Parameter(hidden = true) Language language) {

        return productTypeFacade.get(merchantStore, id, language);

    }

    @GetMapping(value = "/private/product/type/unique", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Verify if product type is unique", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = "DEFAULT")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = "en"))
    })
    public ResponseEntity<EntityExists> exists(@RequestParam String code, @Parameter(hidden = true) MerchantStore merchantStore,
                                               @Parameter(hidden = true) Language language) {

        boolean exists = productTypeFacade.exists(code, merchantStore, language);
        return new ResponseEntity<EntityExists>(new EntityExists(exists), HttpStatus.OK);

    }

    @PostMapping(value = "/private/product/type", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "POST", description = "Create product type", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Entity.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = "DEFAULT")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = "en"))
    })
    public Entity create(@RequestBody PersistableProductType type, @Parameter(hidden = true) MerchantStore merchantStore,
                         @Parameter(hidden = true) Language language) {

        Long id = productTypeFacade.save(type, merchantStore, language);
        Entity entity = new Entity();
        entity.setId(id);
        return entity;

    }

    @PutMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "PUT", description = "Update product type", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = "DEFAULT")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = "en"))
    })
    public void update(@RequestBody PersistableProductType type, @PathVariable Long id,
                       @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        productTypeFacade.update(type, id, merchantStore, language);

    }

    @DeleteMapping(value = "/private/product/type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "DELETE", description = "Delete product type", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = "DEFAULT")),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = "en"))
    })
    public void delete(@PathVariable Long id, @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        productTypeFacade.delete(id, merchantStore, language);

    }

}
