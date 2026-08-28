package com.asrevo.cvhome.merchant.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.merchant.errors.DefaultStoreNotRemovableException;
import com.asrevo.cvhome.merchant.errors.DuplicateMerchantStoreException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreContextMismatchException;
import com.asrevo.cvhome.merchant.errors.MerchantStoreNotFoundException;
import com.asrevo.cvhome.merchant.model.merchant.PersistableMerchantStore;
import com.asrevo.cvhome.merchant.model.merchant.ReadableMerchantStore;
import com.asrevo.cvhome.merchant.service.facade.merchant.StoreFacade;
import com.asrevo.cvhome.store.core.constants.Constants;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Merchant and store management resource (Merchant - Store Management Api)")
@Slf4j
@AllArgsConstructor
public class MerchantStoreApi {

    private final StoreFacade storeFacade;

    @GetMapping(value = {"/store/{code}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get merchant store",
            responses = @ApiResponse(
                    content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    public ReadableMerchantStore store(@PathVariable String code, StoreMerchantId merchantStore,
                                       LanguageCode language)
            throws MerchantStoreNotFoundException, MerchantStoreContextMismatchException {
        StoreMerchantId pathStore = new StoreMerchantId(code);
        if (!pathStore.equals(merchantStore)) {
            throw MerchantStoreContextMismatchException.of(pathStore, merchantStore);
        }
        return storeFacade.getByMerchantStoreId(merchantStore, language);
    }

    @GetMapping(value = {"/private/store"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get merchant store full details",
            responses = @ApiResponse(
                    content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.READ')")
    public ReadableMerchantStore storeFull(StoreMerchantId merchantStore, LanguageCode language)
            throws MerchantStoreNotFoundException {
        return storeFacade.getByMerchantStoreId(merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/store/languages"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get list of store supported languages.", responses = @ApiResponse(
            content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))

    public List<LanguageCode> supportedLanguages(StoreMerchantId merchantStore) {

        return storeFacade.supportedLanguages(merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = {"/private/store"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "POST", description = "Creates a new store",
            responses = @ApiResponse(
                    content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))

    @PreAuthorize("hasPermission(#store.org,'String','STORE-POD.MERCHANT.STORE-CREATE')")
    public void create(@Valid @RequestBody PersistableMerchantStore store) throws DuplicateMerchantStoreException {
        storeFacade.create(store);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/store"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "PUT", description = "Updates a store",
            responses = @ApiResponse(
                    content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
    public void update(StoreMerchantId merchantStore, @Valid @RequestBody PersistableMerchantStore store)
            throws MerchantStoreNotFoundException {
        storeFacade.update(merchantStore, store);
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/store"})
    @Operation(method = "DELETE", description = "Deletes a store",
            responses = @ApiResponse(content = @Content(schema = @Schema())))
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))

    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.MERCHANT.*')")
    public void delete(StoreMerchantId merchantStore)
            throws DefaultStoreNotRemovableException, MerchantStoreNotFoundException {
        storeFacade.delete(merchantStore);
    }

}
