package com.asrevo.cvhome.catalog.api.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;

import com.asrevo.cvhome.catalog.model.product.PersistableProductPrice;
import com.asrevo.cvhome.catalog.model.product.ReadableProductPrice;
import com.asrevo.cvhome.catalog.service.facade.product.ProductPriceFacade;
import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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
    @RequestMapping(
            value = {"/private/product/{sku}/inventory/{inventoryId}/price"},
            method = RequestMethod.POST)
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
    public @ResponseBody Entity save(
            @PathVariable String sku,
            @PathVariable Long inventoryId,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        price.setSku(sku);
        price.setProductAvailabilityId(inventoryId);

        Long id = productPriceFacade.save(price, merchantStore);
        return new Entity(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(
            value = {"/private/product/{sku}/price"},
            method = RequestMethod.POST)
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
    public @ResponseBody Entity save(
            @PathVariable String sku,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        price.setSku(sku);

        Long id = productPriceFacade.save(price, merchantStore);
        return new Entity(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = {"/private/product/{sku}/inventory/{inventoryId}/price/{priceId}"},
            method = RequestMethod.PUT)
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
    public void edit(
            @PathVariable String sku,
            @PathVariable Long inventoryId,
            @PathVariable Long priceId,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        price.setSku(sku);
        price.setProductAvailabilityId(inventoryId);
        price.setId(priceId);
        productPriceFacade.save(price, merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = {"/private/product/{sku}/price/{priceId}"},
            method = RequestMethod.GET)
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
    public ReadableProductPrice get(
            @PathVariable String sku,
            @PathVariable Long priceId,
            @Valid @RequestBody PersistableProductPrice price,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        price.setSku(sku);
        price.setId(priceId);

        return productPriceFacade.get(sku, priceId, merchantStore, language);
    }

    @RequestMapping(
            value = {"/private/product/{sku}/inventory/{inventoryId}/price"},
            method = RequestMethod.GET)
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
    public List<ReadableProductPrice> list(
            @PathVariable String sku,
            @PathVariable Long inventoryId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        return productPriceFacade.list(sku, inventoryId, merchantStore, language);
    }

    @RequestMapping(
            value = {"/private/product/{sku}/prices"},
            method = RequestMethod.GET)
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
    public List<ReadableProductPrice> list(
            @PathVariable String sku,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        return productPriceFacade.list(sku, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = {"/private/product/{sku}/price/{priceId}"},
            method = RequestMethod.DELETE)
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
            @PathVariable String sku,
            @PathVariable Long priceId,
            @Parameter(hidden = true) @SecuredResource StoreMerchantId merchantStore,
            @Parameter(hidden = true) LanguageCode language) {

        productPriceFacade.delete(priceId, sku, merchantStore);
    }
}
