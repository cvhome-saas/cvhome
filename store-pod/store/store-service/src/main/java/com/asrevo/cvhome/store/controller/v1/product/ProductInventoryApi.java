package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.controller.exception.RestApiException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.PersistableInventory;
import com.asrevo.cvhome.store.core.model.catalog.product.inventory.ReadableInventory;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.service.facade.product.ProductInventoryFacade;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product inventory resource (Product Inventory Api)")
public class ProductInventoryApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductInventoryApi.class);
    @Autowired
    private ProductInventoryFacade productInventoryFacade;

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/{productId}/inventory"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableInventory create(@PathVariable Long productId,
                                                  @Valid @RequestBody PersistableInventory inventory, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                                                  @Parameter(hidden = true) Language language) {
        inventory.setProductId(productId);
        return productInventoryFacade.add(inventory, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{productId}/inventory/{id}"}, method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(
            @PathVariable Long productId,
            @PathVariable Long id,
            @Valid @RequestBody PersistableInventory inventory, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        inventory.setId(id);
        inventory.setProductId(inventory.getProductId());
        inventory.setVariant(inventory.getVariant());
        inventory.setProductId(productId);
        productInventoryFacade.update(inventory, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{productId}/inventory/{id}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(
            @PathVariable Long productId,
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        productInventoryFacade.delete(productId, id, merchantStore);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{sku}/inventory"})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableEntityList<ReadableInventory> getBySku(
            @PathVariable String sku,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        return productInventoryFacade.get(sku, merchantStore, language, page, count);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/inventory"})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableEntityList<ReadableInventory> getByProductId(
            @RequestParam Long productId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        if (productId == null) {
            throw new RestApiException("Requires request parameter product id [/product/inventoty?productId");
        }

        return productInventoryFacade.get(productId, merchantStore, language, page, count);

    }

}
