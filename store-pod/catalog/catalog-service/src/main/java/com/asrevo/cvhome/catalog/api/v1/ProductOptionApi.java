package com.asrevo.cvhome.catalog.api.v1;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.DuplicateProductOptionException;
import com.asrevo.cvhome.catalog.errors.ProductOptionInUseException;
import com.asrevo.cvhome.catalog.errors.ProductOptionNotFoundException;
import com.asrevo.cvhome.catalog.model.option.PersistableProductOption;
import com.asrevo.cvhome.catalog.model.option.ReadableProductOption;
import com.asrevo.cvhome.catalog.services.option.ProductOptionService;
import com.asrevo.cvhome.commons.domain.Entity;
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
 * The store's option vocabulary (Color, Size, …), console only. Products assign these options per product; the
 * storefront never reads this API — it sees options through the product responses.
 */
@RestController
@RequestMapping("/api/v1/private/product")
@Tag(name = "Product options")
@RequiredArgsConstructor
public class ProductOptionApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductOptionService productOptionService;

    @GetMapping("/options")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableEntityList<ReadableProductOption> list(StoreMerchantId merchantStore, LanguageCode language,
                                                          Pageable pageable) {
        return productOptionService.list(merchantStore, language, pageable);
    }

    @GetMapping("/option/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableProductOption get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ProductOptionNotFoundException {
        return productOptionService.get(merchantStore, id, language);
    }

    @GetMapping("/option/unique")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public EntityExists exists(@RequestParam String code, StoreMerchantId merchantStore) {
        return new EntityExists(productOptionService.exists(merchantStore, code));
    }

    @PostMapping("/option")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public Entity create(@Valid @RequestBody PersistableProductOption option, StoreMerchantId merchantStore)
            throws DuplicateProductOptionException {
        return new Entity(productOptionService.create(merchantStore, option));
    }

    @PutMapping("/option/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void update(@PathVariable Long id, @Valid @RequestBody PersistableProductOption option,
                       StoreMerchantId merchantStore)
            throws ProductOptionNotFoundException, DuplicateProductOptionException, ProductOptionInUseException {
        productOptionService.update(merchantStore, id, option);
    }

    @DeleteMapping("/option/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore)
            throws ProductOptionNotFoundException, ProductOptionInUseException {
        productOptionService.delete(merchantStore, id);
    }
}
