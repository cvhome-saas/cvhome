package com.asrevo.cvhome.catalog.api.v1;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.CategoryAlreadyAttachedException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.LightPersistableProduct;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The small product writes the console does outside the definition form: the inline switches, delete, the sku
 * check and category membership. The definition itself is {@code ProductApiV2}.
 */
@RestController
@RequestMapping("/api/v1/private/product")
@Tag(name = "Products")
@RequiredArgsConstructor
public class ProductApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductService productService;

    @GetMapping("/unique")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public EntityExists exists(@RequestParam String code, StoreMerchantId merchantStore) {
        return new EntityExists(productService.exists(merchantStore, code));
    }

    @PatchMapping("/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void patch(@PathVariable Long id, @Valid @RequestBody LightPersistableProduct product,
                      StoreMerchantId merchantStore) throws ProductNotFoundException {
        productService.patch(merchantStore, id, product);
    }

    @DeleteMapping("/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore) throws ProductNotFoundException {
        productService.delete(merchantStore, id);
    }

    @PostMapping("/{productId}/category/{categoryId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void addToCategory(@PathVariable Long productId, @PathVariable Long categoryId,
                              StoreMerchantId merchantStore)
            throws ProductNotFoundException, CategoryNotFoundException, CategoryAlreadyAttachedException {
        productService.addToCategory(merchantStore, productId, categoryId);
    }

    @DeleteMapping("/{productId}/category/{categoryId}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void removeFromCategory(@PathVariable Long productId, @PathVariable Long categoryId,
                                   StoreMerchantId merchantStore)
            throws ProductNotFoundException, CategoryNotFoundException {
        productService.removeFromCategory(merchantStore, productId, categoryId);
    }
}
