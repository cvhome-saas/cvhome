package com.asrevo.cvhome.store.controller.v1.category;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.category.PersistableCategory;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategoryList;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ListCriteria;
import com.asrevo.cvhome.store.service.facade.category.CategoryFacade;
import com.asrevo.cvhome.store.service.facade.user.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_STORE;

@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Category management resource (Category Management Api)")
public class CategoryApi {

    private static final int DEFAULT_CATEGORY_DEPTH = 0;

    private final CategoryFacade categoryFacade;

    private final UserFacade userFacade;

    public CategoryApi(CategoryFacade categoryFacade, UserFacade userFacade) {
        this.categoryFacade = categoryFacade;
        this.userFacade = userFacade;
    }

    @GetMapping(value = "/private/category/{id}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category list for an given Category id", summary = "List current Category and child category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of category found")})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCategory get(
            @PathVariable(name = "id") Long categoryId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        ReadableCategory category = categoryFacade.getById(merchantStore, categoryId, language);
        return category;
    }

    @GetMapping(value = "/category/{friendlyUrl}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category list for an given Category code", summary = "List current Category and child category")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of category found")})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCategory getByfriendlyUrl(
            @PathVariable(name = "friendlyUrl") String friendlyUrl,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) throws Exception {
        ReadableCategory category = categoryFacade.getCategoryByFriendlyUrl(merchantStore, friendlyUrl, language);
        return category;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/category/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Check if category code already exists", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code,
                                               @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {
        boolean isCategoryExist = categoryFacade.existByCode(merchantStore, code);
        return new ResponseEntity<EntityExists>(new EntityExists(isCategoryExist), HttpStatus.OK);
    }

    /**
     * Get all category starting from root filter can be used for filtering on
     * fields only featured is supported
     *
     * @return
     */
    @GetMapping(value = "private/category", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category hierarchy from root. Supports filtering FEATURED_CATEGORIES and VISIBLE ONLY by adding ?filter=[featured] or ?filter=[visible] or ? filter=[featured,visible", summary = "Does not return any product attached")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCategoryList list(
            @RequestParam(value = "filter", required = false) List<String> filter,
            @RequestParam(value = "name", required = false) String name,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {


        ListCriteria criteria = new ListCriteria();
        criteria.setName(name);
        return categoryFacade.getReadableCategoryList(merchantStore, criteria, DEFAULT_CATEGORY_DEPTH, language, filter,
                page, count);
    }

    /**
     * Get all category starting from root filter can be used for filtering on
     * fields only featured is supported
     *
     * @return
     */
    @GetMapping(value = "/category", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category hierarchy from root. Supports filtering FEATURED_CATEGORIES and VISIBLE ONLY by adding ?filter=[featured] or ?filter=[visible] or ? filter=[featured,visible", summary = "Does not return any product attached")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCategoryList hierarchyList(
            @RequestParam(value = "filter", required = false) List<String> filter,
            @RequestParam(value = "name", required = false) String name,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {


        ListCriteria criteria = new ListCriteria();
        criteria.setName(name);
        return categoryFacade.getCategoryHierarchy(merchantStore, criteria, DEFAULT_CATEGORY_DEPTH, language, filter,
                page, count);
    }


    @GetMapping(value = "/category/product/{ProductId}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "GET", description = "Get category by product", summary = "")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCategoryList list(
            @PathVariable(name = "ProductId") Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language lang) {


        return categoryFacade.listByProduct(merchantStore, id, lang);

    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/private/category", produces = {APPLICATION_JSON_VALUE})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public PersistableCategory create(
            @Valid @RequestBody PersistableCategory category,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        // superadmin, admin and admin_catalogue
        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of(Constants.GROUP_SUPER_ADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_CATALOGUE, Constants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()));

        return categoryFacade.saveCategory(merchantStore, category);
    }

    @PutMapping(value = "/private/category/{id}", produces = {APPLICATION_JSON_VALUE})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE))
    })
    public PersistableCategory update(@PathVariable Long id, @Valid @RequestBody PersistableCategory category,
                                      @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore) {

        // superadmin, admin and admin_catalogue
        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of(Constants.GROUP_SUPER_ADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_CATALOGUE, Constants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()));


        category.setId(id);
        return categoryFacade.saveCategory(merchantStore, category);
    }

    @PatchMapping(value = "/private/category/{id}/visible", produces = {APPLICATION_JSON_VALUE})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE))
    })
    public void updateVisible(@PathVariable Long id, @Valid @RequestBody PersistableCategory category,
                              @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore
    ) {

        // superadmin, admin and admin_catalogue
        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of(Constants.GROUP_SUPER_ADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_CATALOGUE, Constants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()));

        category.setId(id);
        categoryFacade.setVisible(category, merchantStore);
    }

    @PutMapping(value = "/private/category/{id}/move/{parent}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "PUT", description = "Move a category under another category", summary = "Move category {id} under category {parent}")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE))
    })
    public void move(
            @PathVariable Long id,
            @PathVariable Long parent,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore) {
        // superadmin, admin and admin_catalogue
        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of(Constants.GROUP_SUPER_ADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_CATALOGUE, Constants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()));


        categoryFacade.move(id, parent, merchantStore);
    }

    @DeleteMapping(value = "/private/category/{id}", produces = {APPLICATION_JSON_VALUE})
    @ResponseStatus(OK)
    public void delete(@PathVariable("id") Long categoryId, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore) {

        // superadmin, admin and admin_catalogue
        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of(Constants.GROUP_SUPER_ADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_CATALOGUE, Constants.GROUP_ADMIN_RETAIL).collect(Collectors.toList()));


        categoryFacade.deleteCategory(categoryId, merchantStore);
    }

}
