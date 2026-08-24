package com.asrevo.cvhome.catalog.api.v2;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.billing.commons.errors.EntitlementExceededException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ManufacturerReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductTypeReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductDefinition;
import com.asrevo.cvhome.catalog.model.product.ProductFilter;
import com.asrevo.cvhome.catalog.model.product.ReadableProduct;
import com.asrevo.cvhome.catalog.model.product.ReadableProductDefinition;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The product definition (console) and the product reads the storefront lives on: the listing and the product page.
 * Price and stock come from the inventory service, keyed by sku.
 */
@RestController
@RequestMapping("/api/v2")
@Tag(name = "Products")
@RequiredArgsConstructor
public class ProductApiV2 {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductService productService;

    /**
     * Public, like every storefront read, and also what the console's product table reads: a merchant sees exactly
     * what the shop can, filtered by {@code sku}, {@code available}, {@code categoryIds} and {@code manufacturerId}.
     */
    @GetMapping("/products")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableEntityList<ReadableProduct> list(ProductFilter filter, StoreMerchantId merchantStore,
                                                    LanguageCode language, Pageable pageable) {
        return productService.list(merchantStore, filter, language, pageable);
    }

    @GetMapping("/product/name/{friendlyUrl}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableProduct getByFriendlyUrl(@PathVariable String friendlyUrl, StoreMerchantId merchantStore,
                                            LanguageCode language) throws ProductNotFoundException {
        return productService.getByFriendlyUrl(merchantStore, friendlyUrl, language);
    }

    @GetMapping("/private/product/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableProductDefinition get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ProductNotFoundException {
        return productService.getDefinition(merchantStore, id, language);
    }

    @PostMapping("/private/product")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public Entity create(@Valid @RequestBody PersistableProductDefinition product, StoreMerchantId merchantStore)
            throws ManufacturerReferenceUnresolvableException, ProductTypeReferenceUnresolvableException,
            CategoryReferenceUnresolvableException, EntitlementExceededException {
        return new Entity(productService.create(merchantStore, product));
    }

    @PutMapping("/private/product/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void update(@PathVariable Long id, @Valid @RequestBody PersistableProductDefinition product,
                       StoreMerchantId merchantStore)
            throws ProductNotFoundException, ManufacturerReferenceUnresolvableException,
            ProductTypeReferenceUnresolvableException, CategoryReferenceUnresolvableException {
        productService.update(merchantStore, id, product);
    }
}
