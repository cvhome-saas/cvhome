package com.asrevo.cvhome.catalog.api.v1.category;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

import com.asrevo.cvhome.catalog.model.category.PersistableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategory;
import com.asrevo.cvhome.catalog.model.category.ReadableCategoryList;
import com.asrevo.cvhome.catalog.service.facade.category.CategoryFacade;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1_STR;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Category management resource (Category Management Api)")
public class CategoryApi {

    private static final int DEFAULT_CATEGORY_DEPTH = 0;

    private final CategoryFacade categoryFacade;

    public CategoryApi(CategoryFacade categoryFacade) {
        this.categoryFacade = categoryFacade;
    }

    @GetMapping(value = "/private/category/{id}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category list for an given Category id",
            summary = "List current Category and child category")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "List of category found")})
    @Parameters({
            @Parameter(name = "store",
                    schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang",
                    schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableCategory get(@PathVariable(name = "id") Long categoryId, StoreMerchantId merchantStore,
                                LanguageCode language) {
        return categoryFacade.getById(merchantStore, categoryId, LanguageCode.allLanguage());
    }

    @GetMapping(value = "/category/{friendlyUrl}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category list for an given Category code",
            summary = "List current Category and child category")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "List of category found")})
    @Parameters({
            @Parameter(name = "store",
                    schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang",
                    schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))})

    public ReadableCategory getByFriendlyUrl(@PathVariable(name = "friendlyUrl") String friendlyUrl,
                                             StoreMerchantId merchantStore, LanguageCode language) throws Exception {
        return categoryFacade.getCategoryByFriendlyUrl(merchantStore, friendlyUrl, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/category/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store",
                    schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang",
                    schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))})
    @Operation(method = "GET", description = "Check if category code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code, StoreMerchantId merchantStore,
                                               LanguageCode language) {
        boolean isCategoryExist = categoryFacade.existByCode(merchantStore, code);
        return new ResponseEntity<>(new EntityExists(isCategoryExist), HttpStatus.OK);
    }

    /**
     * Get all categories starting from root filter can be used for filtering on fields
     * only featured is supported
     */
    @GetMapping(value = "/private/category", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET",
            description = "Get category hierarchy from root. Supports filtering FEATURED_CATEGORIES and"
                    + " VISIBLE ONLY by adding ?filter=[featured] or ?filter=[visible] or ?"
                    + " filter=[featured,visible",
            summary = "Does not return any product attached")
    @Parameters({
            @Parameter(name = "store",
                    schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang",
                    schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableCategoryList list(@RequestParam(value = "filter", required = false) List<String> filter,
                                     @RequestParam(value = "name", required = false) String name, StoreMerchantId merchantStore,
                                     LanguageCode language, Pageable pageable) {

        ListCriteria criteria = new ListCriteria();
        criteria.setName(name);
        return categoryFacade.getReadableCategoryList(merchantStore, criteria, DEFAULT_CATEGORY_DEPTH,
                LanguageCode.nonLanguage(), filter, pageable);
    }

    /**
     * Get all categories starting from root filter can be used for filtering on fields
     * only featured is supported
     */
    @GetMapping(value = "/private/category-hierarchy", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET",
            description = "Get category hierarchy from root. Supports filtering FEATURED_CATEGORIES and"
                    + " VISIBLE ONLY by adding ?filter=[featured] or ?filter=[visible] or ?"
                    + " filter=[featured,visible",
            summary = "Does not return any product attached")
    @Parameters({
            @Parameter(name = "store",
                    schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang",
                    schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableCategoryList hierarchyList(@RequestParam(value = "filter", required = false) List<String> filter,
                                              @RequestParam(value = "name", required = false) String name, StoreMerchantId merchantStore,
                                              LanguageCode language, Pageable pageable) {

        ListCriteria criteria = new ListCriteria();
        criteria.setName(name);
        return categoryFacade.getCategoryHierarchy(merchantStore, criteria, DEFAULT_CATEGORY_DEPTH,
                LanguageCode.nonLanguage(), filter, pageable);
    }

    @GetMapping(value = "/category-hierarchy", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET",
            description = "Get category hierarchy from root. Supports filtering FEATURED_CATEGORIES and"
                    + " VISIBLE ONLY by adding ?filter=[featured] or ?filter=[visible] or ?"
                    + " filter=[featured,visible",
            summary = "Does not return any product attached")
    @Parameters({
            @Parameter(name = "store",
                    schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang",
                    schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))})

    public ReadableCategoryList getHierarchyList(@RequestParam(value = "filter", required = false) List<String> filter,
                                                 @RequestParam(value = "name", required = false) String name, StoreMerchantId merchantStore,
                                                 LanguageCode language, Pageable pageable) {

        ListCriteria criteria = new ListCriteria();
        criteria.setName(name);
        return categoryFacade.getCategoryHierarchy(merchantStore, criteria, DEFAULT_CATEGORY_DEPTH, language, filter,
                pageable);
    }

    @GetMapping(value = "/private/category/product/{productId}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category by product")
    @Parameters({
            @Parameter(name = "store",
                    schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR)),
            @Parameter(name = "lang",
                    schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public ReadableCategoryList list(@PathVariable(name = "productId") Long productId, StoreMerchantId merchantStore,
                                     LanguageCode lang) {

        return categoryFacade.listByProduct(merchantStore, productId, LanguageCode.nonLanguage());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/private/category", produces = {APPLICATION_JSON_VALUE})
    @Parameters({@Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public PersistableCategory create(@Valid @RequestBody PersistableCategory category, StoreMerchantId merchantStore) {
        return categoryFacade.saveCategory(merchantStore, category);
    }

    @PutMapping(value = "/private/category/{id}", produces = {APPLICATION_JSON_VALUE})
    @Parameters({@Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public PersistableCategory update(@PathVariable Long id, @Valid @RequestBody PersistableCategory category,
                                      StoreMerchantId merchantStore) {
        category.setId(id);
        return categoryFacade.saveCategory(merchantStore, category);
    }

    @PatchMapping(value = "/private/category/{id}/visible", produces = {APPLICATION_JSON_VALUE})
    @Parameters({@Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void updateVisible(@PathVariable Long id, @Valid @RequestBody PersistableCategory category,
                              StoreMerchantId merchantStore) {

        category.setId(id);
        categoryFacade.setVisible(category, merchantStore);
    }

    @PutMapping(value = "/private/category/{id}/move/{parent}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "PUT", description = "Move a category under another category",
            summary = "Move category {id} under category {parent}")
    @Parameters({@Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))})

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void move(@PathVariable Long id, @PathVariable Long parent, StoreMerchantId merchantStore) {
        categoryFacade.move(id, parent, merchantStore);
    }

    @DeleteMapping(value = "/private/category/{id}", produces = {APPLICATION_JSON_VALUE})
    @ResponseStatus(OK)

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void delete(@PathVariable("id") Long categoryId, StoreMerchantId merchantStore) {
        categoryFacade.deleteCategory(categoryId, merchantStore);
    }

}
