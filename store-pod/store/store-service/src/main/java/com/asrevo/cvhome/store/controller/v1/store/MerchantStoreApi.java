package com.asrevo.cvhome.store.controller.v1.store;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.controller.exception.RestApiException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.content.InputContentFile;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStoreCriteria;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.store.*;
import com.asrevo.cvhome.store.service.facade.store.StoreFacade;
import com.asrevo.cvhome.store.service.facade.user.UserFacade;
import com.asrevo.cvhome.store.utils.ServiceRequestCriteriaBuilderUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Merchant and store management resource (Merchant - Store Management Api)")
@Slf4j
public class MerchantStoreApi {


    private static final Map<String, String> MAPPING_FIELDS = Map.of("name", "name", "readableAudit.user", "auditSection.modifiedBy");

    @Autowired
    private StoreFacade storeFacade;

    @Autowired
    private UserFacade userFacade;

    @GetMapping(value = {"/store/{code}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get merchant store", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    public ReadableMerchantStore store(@PathVariable String code,
                                       @RequestParam(value = "lang", required = false) String lang) {
        //return storeFacade.getByCode(code, lang);
        ReadableMerchantStore readable = storeFacade.getByCode(code, lang);
        return readable;
    }

    @GetMapping(value = {"/private/store/{code}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get merchant store full details", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    @Parameters({
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableMerchantStore storeFull(
            @PathVariable String code,
            @Parameter(hidden = true) Language language) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of("SUPERADMIN", "ADMIN_RETAILER").collect(Collectors.toList()));
        return storeFacade.getFullByCode(code, language);
    }

    @GetMapping(value = {"/private/merchant/{code}/stores"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get retailer child stores", summary = "Merchant (retailer) can have multiple stores",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    @Parameters({
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableMerchantStoreList list(@PathVariable String code, @Parameter(hidden = true) Language language,
                                          @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                          @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of("SUPERADMIN", "ADMIN_RETAILER").collect(Collectors.toList()));

        //ADMIN_RETAILER only see pertaining stores


        return storeFacade.getChildStores(language, code, page, count);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/stores"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get list of stores. Returns all retailers and stores. If superadmin everything is returned, else only retailer and child stores.",
            summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    @Parameters({
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableMerchantStoreList get(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count,
            HttpServletRequest request) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        // requires superadmin, admin and admin retail to see all
        userFacade.authorizedGroup(authenticatedUser,
                Stream.of(Constants.GROUP_SUPER_ADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        MerchantStoreCriteria criteria = createMerchantStoreCriteria(request);

        if (userFacade.userInRoles(authenticatedUser, List.of(Constants.GROUP_SUPER_ADMIN))) {
            criteria.setStoreCode(null);
        } else {
            criteria.setStoreCode(merchantStore.getCode());
        }

        //return storeFacade.findAll(criteria, language, page, count);
        ReadableMerchantStoreList readable = storeFacade.findAll(criteria, language, page, count);
        return readable;
    }


    /**
     * List of store names
     *
     * @param merchantStore
     * @param request
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/stores/names"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get list of store names. Returns all retailers and stores", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    public List<ReadableMerchantStore> list(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count,
            HttpServletRequest request
    ) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        // requires superadmin, admin and admin retail to see all
        userFacade.authorizedGroup(authenticatedUser,
                Stream.of(Constants.GROUP_SUPER_ADMIN, Constants.GROUP_ADMIN, Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        MerchantStoreCriteria criteria = createMerchantStoreCriteria(request);

        if (userFacade.userInRoles(authenticatedUser, List.of(Constants.GROUP_SUPER_ADMIN))) {
            criteria.setStoreCode(null);
        } else {
            criteria.setStoreCode(merchantStore.getCode());
        }

        ReadableMerchantStoreList list = storeFacade.findAll(criteria, language, page, count);
        return list.getData();

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/store/languages"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get list of store supported languages.", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    public List<Language> supportedLanguages(
            @Parameter(hidden = true) MerchantStore merchantStore,
            HttpServletRequest request) {

        return storeFacade.supportedLanguages(merchantStore);
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping(value = {"/private/store"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "POST", description = "Creates a new store", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    public void create(@Valid @RequestBody PersistableMerchantStore store) {


        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(authenticatedUser, Stream.of("SUPERADMIN", "ADMIN_RETAILER").collect(Collectors.toList()));

        store.setOrg("d1952c95-312e-4bb9-9a2d-b703d031276f");
        storeFacade.create(store);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/store/{code}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "PUT", description = "Updates a store", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    public void update(@PathVariable String code, @Valid @RequestBody PersistableMerchantStore store,
                       HttpServletRequest request) {

        String userName = getUserFromRequest(request);
        validateUserPermission(userName, code);
        store.setCode(code);
        storeFacade.update(store);
    }

    private String getUserFromRequest(HttpServletRequest request) {
        // user doing action must be attached to the store being modified
        Principal principal = request.getUserPrincipal();
        return principal.getName();
    }

    private void validateUserPermission(String userName, String code) {
        // TODO reviewed Spring Security should be used
        if (!userFacade.authorizedStore(userName, code)) {
            throw new UnauthorizedException("User " + userName + " not authorized");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/store/{code}/marketing"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get store branding and marketing details", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableBrand.class))))
    public ReadableBrand getStoreMarketing(@PathVariable String code, HttpServletRequest request) {
        String userName = getUserFromRequest(request);
        validateUserPermission(userName, code);
        return storeFacade.getBrand(code);
    }

    /**
     * List child stores
     *
     * @param code
     * @param request
     * @return
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/merchant/{code}/children"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Get child stores", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = ReadableMerchantStore.class))))
    @Parameters({
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableMerchantStoreList children(@PathVariable String code, @Parameter(hidden = true) Language language,
                                              @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
                                              @RequestParam(value = "count", required = false, defaultValue = "10") Integer count,
                                              HttpServletRequest request) {

        String userName = getUserFromRequest(request);
        validateUserPermission(userName, code);
        return storeFacade.getChildStores(language, code, page, count);

    }

    @Deprecated
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/store/{code}/marketing"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "POST", description = "Create or save store branding and marketing details", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Void.class))))
    public void saveStoreMarketing(@PathVariable String code, @RequestBody PersistableBrand brand,
                                   HttpServletRequest request) {
        String userName = getUserFromRequest(request);
        validateUserPermission(userName, code);
        storeFacade.createBrand(code, brand);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/store/{code}/marketing/logo"})
    @Operation(method = "POST", description = "Add store logo", summary = "")
    public void addLogo(@PathVariable String code, @RequestParam("file") MultipartFile uploadfile,
                        HttpServletRequest request) {

        // user doing action must be attached to the store being modified
        String userName = getUserFromRequest(request);

        validateUserPermission(userName, code);

        if (uploadfile.isEmpty()) {
            throw new RestApiException("Upload file is empty");
        }

        InputContentFile cmsContentImage = createInputContentFile(uploadfile);
        storeFacade.addStoreLogo(code, cmsContentImage);
    }

    private InputContentFile createInputContentFile(MultipartFile image) {

        InputContentFile cmsContentImage = null;

        try {

            InputStream input = new ByteArrayInputStream(image.getBytes());
            cmsContentImage = new InputContentFile();
            cmsContentImage.setFileName(image.getOriginalFilename());
            cmsContentImage.setMimeType(image.getContentType());
            cmsContentImage.setFileContentType(FileContentType.LOGO);
            cmsContentImage.setFile(input);

        } catch (IOException ioe) {
            throw new RestApiException(ioe);
        }

        return cmsContentImage;
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/store/{code}/marketing/logo"})
    @Operation(method = "DELETE", description = "Delete store logo", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Void.class))))
    public void deleteStoreLogo(@PathVariable String code, HttpServletRequest request) {

        // user doing action must be attached to the store being modified
        String userName = getUserFromRequest(request);
        validateUserPermission(userName, code);

        // delete store logo
        storeFacade.deleteLogo(code);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/store/unique", "/private/store/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(method = "GET", description = "Check if store code already exists", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class))))
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code) {
        boolean isStoreExist = storeFacade.existByCode(code);
        return new ResponseEntity<EntityExists>(new EntityExists(isStoreExist), HttpStatus.OK);
    }


    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/store/{code}"})
    @Operation(method = "DELETE", description = "Deletes a store", summary = "", responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Void.class))))
    public void delete(@PathVariable String code, HttpServletRequest request) {
        String userName = getUserFromRequest(request);
        validateUserPermission(userName, code);
        storeFacade.delete(code);
    }


    private MerchantStoreCriteria createMerchantStoreCriteria(HttpServletRequest request) {
        try {
            return (MerchantStoreCriteria) ServiceRequestCriteriaBuilderUtils.buildRequestCriterias(new MerchantStoreCriteria(), MAPPING_FIELDS,
                    request);
        } catch (Exception e) {
            throw new RestApiException("Error while binding request parameters");
        }

    }


}
