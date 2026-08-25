package com.asrevo.cvhome.catalog.api.v1;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.catalog.errors.CategoryFriendlyUrlNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.CategoryReferenceUnresolvableException;
import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.services.category.CategoryService;
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
 * The category tree. Private reads answer every language for the console; the storefront reads answer the
 * shopper's.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Categories")
@RequiredArgsConstructor
public class CategoryApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final CategoryService categoryService;

    @GetMapping("/category-hierarchy")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableEntityList<ReadableCategory> hierarchy(@RequestParam(required = false) String name,
                                                          StoreMerchantId merchantStore, LanguageCode language,
                                                          Pageable pageable) {
        return categoryService.hierarchy(merchantStore, name, language, false, pageable);
    }

    @GetMapping("/category/{friendlyUrl}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public ReadableCategory getByFriendlyUrl(@PathVariable String friendlyUrl, StoreMerchantId merchantStore,
                                             LanguageCode language) throws CategoryFriendlyUrlNotFoundException {
        return categoryService.getByFriendlyUrl(merchantStore, friendlyUrl, language);
    }

    @GetMapping("/private/category-hierarchy")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableEntityList<ReadableCategory> privateHierarchy(@RequestParam(required = false) String name,
                                                                 StoreMerchantId merchantStore,
                                                                 LanguageCode language, Pageable pageable) {
        return categoryService.hierarchy(merchantStore, name, language, true, pageable);
    }

    @GetMapping("/private/category")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableEntityList<ReadableCategory> list(@RequestParam(required = false) String name,
                                                     StoreMerchantId merchantStore, LanguageCode language,
                                                     Pageable pageable) {
        return categoryService.list(merchantStore, name, language, true, pageable);
    }

    @GetMapping("/private/category/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableCategory get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws CategoryNotFoundException {
        return categoryService.get(merchantStore, id, language);
    }

    @GetMapping("/private/category/product/{productId}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableEntityList<ReadableCategory> listByProduct(@PathVariable Long productId,
                                                              StoreMerchantId merchantStore, LanguageCode language) {
        return categoryService.listByProduct(merchantStore, productId, language);
    }

    @GetMapping("/private/category/unique")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public EntityExists exists(@RequestParam String code, StoreMerchantId merchantStore) {
        return new EntityExists(categoryService.exists(merchantStore, code));
    }

    @PostMapping("/private/category")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public PersistableCategory create(@Valid @RequestBody PersistableCategory category, StoreMerchantId merchantStore)
            throws CategoryNotFoundException, CategoryReferenceUnresolvableException {
        category.setId(null);
        return categoryService.save(merchantStore, category);
    }

    @PutMapping("/private/category/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public PersistableCategory update(@PathVariable Long id, @Valid @RequestBody PersistableCategory category,
                                      StoreMerchantId merchantStore)
            throws CategoryNotFoundException, CategoryReferenceUnresolvableException {
        category.setId(id);
        return categoryService.save(merchantStore, category);
    }

    @PatchMapping("/private/category/{id}/visible")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void setVisible(@PathVariable Long id, @RequestBody PersistableCategory category,
                           StoreMerchantId merchantStore) throws CategoryNotFoundException {
        categoryService.setVisible(merchantStore, id, category.isVisible());
    }

    @PutMapping("/private/category/{id}/move/{parent}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void move(@PathVariable Long id, @PathVariable Long parent, StoreMerchantId merchantStore)
            throws CategoryNotFoundException {
        categoryService.move(merchantStore, id, parent);
    }

    @DeleteMapping("/private/category/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore) throws CategoryNotFoundException {
        categoryService.delete(merchantStore, id);
    }
}
