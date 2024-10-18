package com.asrevo.cvhome.store.controller.v1.catalog;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalog;
import com.asrevo.cvhome.store.core.model.catalog.catalog.PersistableCatalogCategoryEntry;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalog;
import com.asrevo.cvhome.store.core.model.catalog.catalog.ReadableCatalogCategoryEntry;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.service.facade.catalog.CatalogFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1")
@Tag(name = "Catalog management resource (Catalog Management Api)")
@Slf4j
public class CatalogApi {

    private final CatalogFacade catalogFacade;

    public CatalogApi(CatalogFacade catalogFacade) {
        this.catalogFacade = catalogFacade;
    }

    @GetMapping(value = "/private/catalogs")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "GET",
            description = "Get catalogs by merchant",
            responses =
                    @ApiResponse(
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    ReadableEntityList.class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableEntityList<ReadableCatalog> getCatalogs(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            Optional<String> code,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        return catalogFacade.getListCatalogs(code, merchantStore, language, page, count);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(
            value = {"/private/catalog/unique"},
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(
            method = "GET",
            description = "Check if catalog code already exists",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    public ResponseEntity<EntityExists> exists(
            @RequestParam(value = "code") String code,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        boolean existByCode = catalogFacade.uniqueCatalog(code, merchantStore);
        return new ResponseEntity<>(new EntityExists(existByCode), HttpStatus.OK);
    }

    @PostMapping(value = "/private/catalog")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "POST",
            description = "Create catalog",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCatalog createCatalog(
            @RequestBody @Valid PersistableCatalog catalog,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        return catalogFacade.saveCatalog(catalog, merchantStore, language);
    }

    @PatchMapping(value = "/private/catalog/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "PATCH",
            description = "Update catalog",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updateCatalog(
            @PathVariable Long id,
            @RequestBody @Valid PersistableCatalog catalog,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        catalog.setId(id);
        catalogFacade.updateCatalog(id, catalog, merchantStore, language);
    }

    @GetMapping(value = "/private/catalog/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "GET",
            description = "Get catalog",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCatalog getCatalog(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        return catalogFacade.getCatalog(id, merchantStore, language);
    }

    @DeleteMapping(value = "/private/catalog/{id}")
    @Operation(
            method = "DELETE",
            description = "Deletes a catalog",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteCatalog(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        catalogFacade.deleteCatalog(id, merchantStore, language);
    }

    @PostMapping(value = "/private/catalog/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "POST",
            description = "Add catalog entry to catalog",
            responses =
                    @ApiResponse(
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    ReadableCatalogCategoryEntry
                                                                            .class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableCatalogCategoryEntry addCatalogEntry(
            @PathVariable Long id,
            @RequestBody @Valid PersistableCatalogCategoryEntry catalogEntry,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        ReadableCatalog c = catalogFacade.getCatalog(id, merchantStore, language);

        if (c == null) {
            throw new ResourceNotFoundException("Catalog id [" + id + "] not found");
        }

        catalogEntry.setCatalog(c.getCode());
        return catalogFacade.addCatalogEntry(catalogEntry, merchantStore, language);
    }

    @DeleteMapping(value = "/private/catalog/{id}/entry/{entryId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "DELETE",
            description = "Remove catalog entry from catalog",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void removeCatalogEntry(
            @PathVariable Long id,
            @PathVariable Long entryId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        catalogFacade.removeCatalogEntry(id, entryId, merchantStore, language);
    }

    @GetMapping(value = "/private/catalog/{id}/entry")
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "GET",
            description = "Get catalog entry by catalog",
            responses =
                    @ApiResponse(
                            content =
                                    @Content(
                                            schema =
                                                    @Schema(
                                                            implementation =
                                                                    ReadableEntityList.class))))
    @Parameters({
        @Parameter(
                name = "store",
                schema =
                        @Schema(
                                name = "store",
                                type = "string",
                                defaultValue = DEFAULT_ORG1_STORE1)),
        @Parameter(
                name = "lang",
                schema =
                        @Schema(
                                name = "lang",
                                type = "string",
                                defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableEntityList<ReadableCatalogCategoryEntry> getCatalogEntry(
            @PathVariable(value = "id") Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count,
            HttpServletRequest request) {

        return catalogFacade.listCatalogEntry(
                catalogEntryFilter(request), id, merchantStore, language, page, count);
    }

    private Optional<String> catalogFilter(HttpServletRequest request) {

        return Optional.ofNullable((String) request.getAttribute("code"));
    }

    private Optional<String> catalogEntryFilter(HttpServletRequest request) {

        return Optional.ofNullable((String) request.getAttribute("name"));
    }
}
