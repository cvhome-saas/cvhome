package com.asrevo.cvhome.catalog.api.v1;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.ProductGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.group.PersistableProductGroup;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.services.group.ProductGroupService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * The store's merchandising strips. The storefront reads one by code; the console manages them.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product groups")
@RequiredArgsConstructor
public class ProductGroupApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductGroupService productGroupService;

    @GetMapping("/products/groups/{code}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableProductGroup get(@PathVariable String code, StoreMerchantId merchantStore, LanguageCode language) {
        return productGroupService.storefront(merchantStore, code, language);
    }

    @GetMapping("/private/products/groups/{code}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableProductGroup getPrivate(@PathVariable String code, StoreMerchantId merchantStore,
                                           LanguageCode language) throws ProductGroupNotFoundException {
        return productGroupService.get(merchantStore, code, language, true);
    }

    @GetMapping("/private/products/groups")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableEntityList<ReadableProductGroup> list(StoreMerchantId merchantStore, LanguageCode language,
                                                         Pageable pageable) {
        return productGroupService.list(merchantStore, language, pageable);
    }

    @GetMapping("/private/products/groups/unique")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public EntityExists exists(@RequestParam String code, StoreMerchantId merchantStore) {
        return new EntityExists(productGroupService.exists(merchantStore, code));
    }

    @PostMapping("/private/products/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public PersistableProductGroup save(@Valid @RequestBody PersistableProductGroup group,
                                        StoreMerchantId merchantStore)
            throws ProductGroupNotFoundException, ProductNotFoundException {
        return productGroupService.save(merchantStore, group);
    }

    @DeleteMapping("/private/products/groups/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable String code, StoreMerchantId merchantStore)
            throws ProductGroupNotFoundException {
        productGroupService.delete(merchantStore, code);
    }

    @PostMapping("/private/products/groups/{code}/product/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void addProduct(@PathVariable String code, @PathVariable Long productId, StoreMerchantId merchantStore)
            throws ProductGroupNotFoundException, ProductNotFoundException {
        productGroupService.addProduct(merchantStore, code, productId);
    }

    @DeleteMapping("/private/products/groups/{code}/product/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void removeProduct(@PathVariable String code, @PathVariable Long productId,
                              StoreMerchantId merchantStore) throws ProductGroupNotFoundException {
        productGroupService.removeProduct(merchantStore, code, productId);
    }
}
