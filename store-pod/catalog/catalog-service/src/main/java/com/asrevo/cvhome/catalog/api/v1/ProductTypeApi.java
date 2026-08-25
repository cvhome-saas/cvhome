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

import com.asrevo.cvhome.catalog.errors.DuplicateProductTypeException;
import com.asrevo.cvhome.catalog.errors.ProductTypeNotFoundException;
import com.asrevo.cvhome.catalog.model.type.PersistableProductType;
import com.asrevo.cvhome.catalog.model.type.ReadableProductType;
import com.asrevo.cvhome.catalog.services.type.ProductTypeService;
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
 * Product types, console only.
 */
@RestController
@RequestMapping("/api/v1/private/product")
@Tag(name = "Product types")
@RequiredArgsConstructor
public class ProductTypeApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductTypeService productTypeService;

    @GetMapping("/types")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableEntityList<ReadableProductType> list(StoreMerchantId merchantStore, LanguageCode language,
                                                        Pageable pageable) {
        return productTypeService.list(merchantStore, language, pageable);
    }

    @GetMapping("/type/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableProductType get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ProductTypeNotFoundException {
        return productTypeService.get(merchantStore, id, language);
    }

    @GetMapping("/type/unique")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public EntityExists exists(@RequestParam String code, StoreMerchantId merchantStore) {
        return new EntityExists(productTypeService.exists(merchantStore, code));
    }

    @PostMapping("/type")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public Entity create(@Valid @RequestBody PersistableProductType type, StoreMerchantId merchantStore)
            throws DuplicateProductTypeException {
        return new Entity(productTypeService.create(merchantStore, type));
    }

    @PutMapping("/type/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void update(@PathVariable Long id, @Valid @RequestBody PersistableProductType type,
                       StoreMerchantId merchantStore) throws ProductTypeNotFoundException {
        productTypeService.update(merchantStore, id, type);
    }

    @DeleteMapping("/type/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore) throws ProductTypeNotFoundException {
        productTypeService.delete(merchantStore, id);
    }
}
