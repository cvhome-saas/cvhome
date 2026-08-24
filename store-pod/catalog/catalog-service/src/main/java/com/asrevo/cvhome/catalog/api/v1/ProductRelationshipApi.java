package com.asrevo.cvhome.catalog.api.v1;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.ProductGroupNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.group.ReadableProductGroup;
import com.asrevo.cvhome.catalog.services.group.ProductGroupService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * A product's related items — the "you may also like" strip on the product page.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product relationships")
@RequiredArgsConstructor
public class ProductRelationshipApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductGroupService productGroupService;

    @GetMapping("/products/{id}/relationship")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableProductGroup related(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ProductGroupNotFoundException {
        return productGroupService.related(merchantStore, id, language);
    }

    @PostMapping("/private/products/{id}/relationship/{productId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void add(@PathVariable Long id, @PathVariable Long productId, StoreMerchantId merchantStore)
            throws ProductNotFoundException {
        productGroupService.addRelated(merchantStore, id, productId);
    }

    @DeleteMapping("/private/products/{id}/relationship/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void remove(@PathVariable Long id, @PathVariable Long productId, StoreMerchantId merchantStore)
            throws ProductGroupNotFoundException {
        productGroupService.removeRelated(merchantStore, id, productId);
    }
}
