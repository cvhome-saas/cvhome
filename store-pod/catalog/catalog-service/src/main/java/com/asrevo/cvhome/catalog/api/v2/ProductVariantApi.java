package com.asrevo.cvhome.catalog.api.v2;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.DuplicateVariantCombinationException;
import com.asrevo.cvhome.catalog.errors.DuplicateVariantSkuException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.errors.VariantLimitExceededException;
import com.asrevo.cvhome.catalog.errors.VariantOptionsInvalidException;
import com.asrevo.cvhome.catalog.model.product.PersistableVariantSet;
import com.asrevo.cvhome.catalog.model.product.ReadableProductVariantDefinition;
import com.asrevo.cvhome.catalog.services.variant.ProductVariantService;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * A product's variant set, console only. One atomic whole-set replace: the payload names the axes (option codes)
 * and the combinations together, so they can never disagree — and a product with axes always has combinations by
 * construction, which is why no separate publishability check exists.
 */
@RestController
@RequestMapping("/api/v2/private/product")
@Tag(name = "Product variants")
@RequiredArgsConstructor
public class ProductVariantApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductVariantService productVariantService;

    @GetMapping("/{id}/variants")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public List<ReadableProductVariantDefinition> list(@PathVariable Long id, StoreMerchantId merchantStore,
                                                       LanguageCode language) throws ProductNotFoundException {
        return productVariantService.list(merchantStore, id, language);
    }

    @PutMapping("/{id}/variants")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void replace(@PathVariable Long id, @Valid @RequestBody PersistableVariantSet set,
                        StoreMerchantId merchantStore)
            throws ProductNotFoundException, ProductOptionNotFoundException, VariantOptionsInvalidException,
            DuplicateVariantSkuException, DuplicateVariantCombinationException, VariantLimitExceededException {
        productVariantService.replaceAll(merchantStore, id, set);
    }
}
