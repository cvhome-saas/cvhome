package com.asrevo.cvhome.inventory.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.errors.InventoryReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotConvertibleException;
import com.asrevo.cvhome.inventory.errors.ProductPriceNotFoundException;
import com.asrevo.cvhome.inventory.errors.SkuReferenceUnresolvableException;
import com.asrevo.cvhome.inventory.model.price.PersistableProductPrice;
import com.asrevo.cvhome.inventory.model.price.ReadableProductPrice;
import com.asrevo.cvhome.inventory.service.facade.ProductPriceFacade;
import com.asrevo.cvhome.store.core.constants.Constants;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * Price CRUD, moved out of the catalog service with its paths preserved.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product price api")
@Slf4j
public class ProductPriceApi {

    private final ProductPriceFacade productPriceFacade;

    public ProductPriceApi(ProductPriceFacade productPriceFacade) {
        this.productPriceFacade = productPriceFacade;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = {"/private/product/{sku}/inventory/{inventoryId}/price"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public Entity save(@PathVariable String sku, @PathVariable Long inventoryId,
                       @Valid @RequestBody PersistableProductPrice price, StoreMerchantId merchantStore,
                       LanguageCode language)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            SkuReferenceUnresolvableException {

        price.setSku(sku);
        price.setProductAvailabilityId(inventoryId);

        Long id = productPriceFacade.save(price, merchantStore);
        return new Entity(id);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/{sku}/price"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public Entity save(@PathVariable String sku, @Valid @RequestBody PersistableProductPrice price,
                       StoreMerchantId merchantStore, LanguageCode language)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            SkuReferenceUnresolvableException {

        price.setSku(sku);

        Long id = productPriceFacade.save(price, merchantStore);
        return new Entity(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/{sku}/inventory/{inventoryId}/price/{priceId}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public void edit(@PathVariable String sku, @PathVariable Long inventoryId, @PathVariable Long priceId,
                     @Valid @RequestBody PersistableProductPrice price, StoreMerchantId merchantStore,
                     LanguageCode language)
            throws ProductPriceNotConvertibleException, InventoryReferenceUnresolvableException,
            SkuReferenceUnresolvableException {

        price.setSku(sku);
        price.setProductAvailabilityId(inventoryId);
        price.setId(priceId);
        productPriceFacade.save(price, merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{sku}/price/{priceId}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public ReadableProductPrice get(@PathVariable String sku, @PathVariable Long priceId,
                                    StoreMerchantId merchantStore, LanguageCode language)
            throws ProductPriceNotFoundException, ProductPriceNotConvertibleException {

        return productPriceFacade.get(sku, priceId, merchantStore, language);
    }

    @GetMapping(value = {"/private/product/{sku}/inventory/{inventoryId}/price"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public List<ReadableProductPrice> list(@PathVariable String sku, @PathVariable Long inventoryId,
                                           StoreMerchantId merchantStore, LanguageCode language)
            throws ProductPriceNotConvertibleException {

        return productPriceFacade.list(sku, inventoryId, merchantStore, language);
    }

    @GetMapping(value = {"/private/product/{sku}/prices"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public List<ReadableProductPrice> list(@PathVariable String sku, StoreMerchantId merchantStore,
                                           LanguageCode language)
            throws ProductPriceNotConvertibleException {

        return productPriceFacade.list(sku, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/{sku}/price/{priceId}"})
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.INVENTORY.*')")
    public void delete(@PathVariable String sku, @PathVariable Long priceId, StoreMerchantId merchantStore,
                       LanguageCode language)
            throws ProductPriceNotFoundException {

        productPriceFacade.delete(priceId, sku, merchantStore);
    }

}
