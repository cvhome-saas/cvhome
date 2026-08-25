package com.asrevo.cvhome.catalog.api.v1;

import java.util.List;

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

import com.asrevo.cvhome.catalog.errors.CategoryNotFoundException;
import com.asrevo.cvhome.catalog.errors.ManufacturerNotFoundException;
import com.asrevo.cvhome.catalog.model.manufacturer.PersistableManufacturer;
import com.asrevo.cvhome.catalog.model.manufacturer.ReadableManufacturer;
import com.asrevo.cvhome.catalog.services.manufacturer.ManufacturerService;
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
 * Brands. The console manages them; the storefront reads the brands present in a category as a filter facet.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Manufacturers (brands)")
@RequiredArgsConstructor
public class ManufacturerApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ManufacturerService manufacturerService;

    @GetMapping("/category/{id}/manufacturer")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<ReadableManufacturer> listByCategory(@PathVariable Long id, StoreMerchantId merchantStore,
                                                     LanguageCode language) throws CategoryNotFoundException {
        return manufacturerService.listByCategory(merchantStore, id, language);
    }

    @GetMapping("/private/manufacturers")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableEntityList<ReadableManufacturer> list(@RequestParam(required = false) String name,
                                                         StoreMerchantId merchantStore, LanguageCode language,
                                                         Pageable pageable) {
        return manufacturerService.list(merchantStore, name, language, pageable);
    }

    @GetMapping("/private/manufacturer/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public ReadableManufacturer get(@PathVariable Long id, StoreMerchantId merchantStore, LanguageCode language)
            throws ManufacturerNotFoundException {
        return manufacturerService.get(merchantStore, id, language);
    }

    @GetMapping("/private/manufacturer/unique")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public EntityExists exists(@RequestParam String code, StoreMerchantId merchantStore) {
        return new EntityExists(manufacturerService.exists(merchantStore, code));
    }

    @PostMapping("/private/manufacturer")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public PersistableManufacturer create(@Valid @RequestBody PersistableManufacturer manufacturer,
                                          StoreMerchantId merchantStore) throws ManufacturerNotFoundException {
        manufacturer.setId(null);
        manufacturer.setId(manufacturerService.save(merchantStore, manufacturer));
        return manufacturer;
    }

    @PutMapping("/private/manufacturer/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void update(@PathVariable Long id, @Valid @RequestBody PersistableManufacturer manufacturer,
                       StoreMerchantId merchantStore) throws ManufacturerNotFoundException {
        manufacturer.setId(id);
        manufacturerService.save(merchantStore, manufacturer);
    }

    @DeleteMapping("/private/manufacturer/{id}")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable Long id, StoreMerchantId merchantStore) throws ManufacturerNotFoundException {
        manufacturerService.delete(merchantStore, id);
    }
}
