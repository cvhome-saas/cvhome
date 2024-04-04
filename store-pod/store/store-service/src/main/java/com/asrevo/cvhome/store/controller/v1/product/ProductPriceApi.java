package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductPrice;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import com.asrevo.cvhome.store.service.facade.product.ProductPriceFacade;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Use inventory
 *
 * @author carlsamson
 */

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product price api")
@Slf4j
public class ProductPriceApi {


    private final ProductPriceFacade productPriceFacade;

    public ProductPriceApi(ProductPriceFacade productPriceFacade) {
        this.productPriceFacade = productPriceFacade;
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{sku}/inventory/{inventoryId}/price"},
            method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody Entity save(
            @PathVariable String sku,
            @PathVariable Long inventoryId,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        price.setSku(sku);
        price.setProductAvailabilityId(inventoryId);

        Long id = productPriceFacade.save(price, merchantStore);
        return new Entity(id);


    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/{sku}/price"},
            method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody Entity save(
            @PathVariable String sku,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        price.setSku(sku);

        Long id = productPriceFacade.save(price, merchantStore);
        return new Entity(id);


    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{sku}/inventory/{inventoryId}/price/{priceId}"},
            method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void edit(
            @PathVariable String sku,
            @PathVariable Long inventoryId,
            @PathVariable Long priceId,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {


        price.setSku(sku);
        price.setProductAvailabilityId(inventoryId);
        price.setId(priceId);
        productPriceFacade.save(price, merchantStore);


    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{sku}/price/{priceId}"},
            method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductPrice get(
            @PathVariable String sku,
            @PathVariable Long priceId,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {


        price.setSku(sku);
        price.setId(priceId);

        return productPriceFacade.get(sku, priceId, merchantStore, language);

    }

    @RequestMapping(value = {"/private/product/{sku}/inventory/{inventoryId}/price"},
            method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableProductPrice> list(
            @PathVariable String sku,
            @PathVariable Long inventoryId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {


        return productPriceFacade.list(sku, inventoryId, merchantStore, language);


    }


    @RequestMapping(value = {"/private/product/{sku}/prices"},
            method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableProductPrice> list(
            @PathVariable String sku,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {


        return productPriceFacade.list(sku, merchantStore, language);


    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{sku}/price/{priceId}"},
            method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(
            @PathVariable String sku,
            @PathVariable Long priceId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {


        productPriceFacade.delete(priceId, sku, merchantStore);

    }

}
