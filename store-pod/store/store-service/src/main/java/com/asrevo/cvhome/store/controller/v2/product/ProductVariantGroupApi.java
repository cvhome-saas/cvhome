package com.asrevo.cvhome.store.controller.v2.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.PersistableProductVariantGroup;
import com.asrevo.cvhome.store.core.model.catalog.product.product.variantGroup.ReadableProductVariantGroup;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.service.facade.product.ProductVariantGroupFacade;
import com.asrevo.cvhome.store.service.facade.user.UserFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.validation.Valid;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/api/v2")
@Tags(value = @Tag(name = "Product instances group api"))
public class ProductVariantGroupApi {

    private final ProductVariantGroupFacade productVariantGroupFacade;

    private final UserFacade userFacade;

    public ProductVariantGroupApi(
            ProductVariantGroupFacade productVariantGroupFacade, UserFacade userFacade) {
        this.productVariantGroupFacade = productVariantGroupFacade;
        this.userFacade = userFacade;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product/productVariantGroup"})
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
    public @ResponseBody Entity create(
            @Valid @RequestBody PersistableProductVariantGroup instanceGroup,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(
                authenticatedUser,
                Stream.of(
                                Constants.GROUP_SUPER_ADMIN,
                                Constants.GROUP_ADMIN,
                                Constants.GROUP_ADMIN_CATALOGUE,
                                Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        Long id = productVariantGroupFacade.create(instanceGroup, merchantStore, language);

        return new Entity(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/productVariantGroup/{id}"})
    @Operation(
            method = "PUT",
            description = "Update product instance group",
            responses =
                    @ApiResponse(
                            content = @Content(mediaType = "application/json", schema = @Schema())))
    public @ResponseBody void update(
            @PathVariable Long id,
            @Valid @RequestBody PersistableProductVariantGroup instance,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(
                authenticatedUser,
                Stream.of(
                                Constants.GROUP_SUPER_ADMIN,
                                Constants.GROUP_ADMIN,
                                Constants.GROUP_ADMIN_CATALOGUE,
                                Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        productVariantGroupFacade.update(id, instance, merchantStore, language);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/productVariantGroup/{id}"})
    @Operation(
            method = "GET",
            description = "Get product instance group",
            responses =
                    @ApiResponse(
                            content = @Content(mediaType = "application/json", schema = @Schema())))
    public @ResponseBody ReadableProductVariantGroup get(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(
                authenticatedUser,
                Stream.of(
                                Constants.GROUP_SUPER_ADMIN,
                                Constants.GROUP_ADMIN,
                                Constants.GROUP_ADMIN_CATALOGUE,
                                Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        return productVariantGroupFacade.get(id, merchantStore, language);
    }

    // delete

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/productVariantGroup/{id}"})
    @Operation(
            method = "DELETE",
            description = "Delete product instance group",
            responses =
                    @ApiResponse(
                            content = @Content(mediaType = "application/json", schema = @Schema())))
    public @ResponseBody void delete(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(
                authenticatedUser,
                Stream.of(
                                Constants.GROUP_SUPER_ADMIN,
                                Constants.GROUP_ADMIN,
                                Constants.GROUP_ADMIN_CATALOGUE,
                                Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        productVariantGroupFacade.delete(id, id, merchantStore);
    }

    // list
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{id}/productVariantGroup"})
    @Operation(
            method = "GET",
            description = "Delete product instance group",
            responses =
                    @ApiResponse(
                            content = @Content(mediaType = "application/json", schema = @Schema())))
    public @ResponseBody ReadableEntityList<ReadableProductVariantGroup> list(
            @PathVariable final Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(
                authenticatedUser,
                Stream.of(
                                Constants.GROUP_SUPER_ADMIN,
                                Constants.GROUP_ADMIN,
                                Constants.GROUP_ADMIN_CATALOGUE,
                                Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        return productVariantGroupFacade.list(id, merchantStore, language, page, count);
    }

    // add image
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(
            value = {"/private/product/productVariantGroup/{id}/image"},
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE},
            method = RequestMethod.POST)
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
    public void addImage(
            @PathVariable Long id,
            @RequestParam(value = "file") MultipartFile file,
            @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(
                authenticatedUser,
                Stream.of(
                                Constants.GROUP_SUPER_ADMIN,
                                Constants.GROUP_ADMIN,
                                Constants.GROUP_ADMIN_CATALOGUE,
                                Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        productVariantGroupFacade.addImage(file, id, merchantStore, language);
    }

    // remove image
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(
            value = {"/private/product/productVariantGroup/{id}/image/{imageId}"},
            method = RequestMethod.DELETE)
    public void removeImage(
            @PathVariable Long id,
            @PathVariable Long imageId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        String authenticatedUser = userFacade.authenticatedUser();
        if (authenticatedUser == null) {
            throw new UnauthorizedException();
        }

        userFacade.authorizedGroup(
                authenticatedUser,
                Stream.of(
                                Constants.GROUP_SUPER_ADMIN,
                                Constants.GROUP_ADMIN,
                                Constants.GROUP_ADMIN_CATALOGUE,
                                Constants.GROUP_ADMIN_RETAIL)
                        .collect(Collectors.toList()));

        productVariantGroupFacade.removeImage(imageId, id, merchantStore);
    }
}
