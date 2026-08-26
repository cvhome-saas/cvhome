package com.asrevo.cvhome.catalog.api.v1;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
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

import com.asrevo.cvhome.catalog.errors.ProductImageAssetUnknownException;
import com.asrevo.cvhome.catalog.errors.ProductImageNotFoundException;
import com.asrevo.cvhome.catalog.errors.ProductNotFoundException;
import com.asrevo.cvhome.catalog.model.product.PersistableProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.services.image.ProductImageService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.errors.RemoteServiceTimeoutException;
import com.asrevo.cvhome.errors.RemoteServiceUnavailableException;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

/**
 * A product's gallery: references into the content service's media library.
 *
 * <p>
 * There is no upload here any more. The seller puts bytes in the media library — where they are deduplicated,
 * measured, given alt text and protected from deletion while something shows them — and attaches asset ids.
 * </p>
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product images")
@RequiredArgsConstructor
public class ProductImageApi {

    private static final String MANAGE = "hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')";

    private final ProductImageService productImageService;

    @GetMapping("/product/{productId}/images")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    public List<ReadableImage> list(@PathVariable Long productId, StoreMerchantId merchantStore)
            throws ProductNotFoundException {
        return productImageService.list(merchantStore, productId);
    }

    /**
     * Appends images after the ones the product already has.
     */
    @PostMapping("/private/product/{id}/images")
    @ResponseStatus(HttpStatus.CREATED)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public List<ReadableImage> attach(@PathVariable Long id,
                                      @RequestBody @Valid List<PersistableProductImage> body,
                                      StoreMerchantId merchantStore)
            throws ProductNotFoundException, ProductImageAssetUnknownException, RemoteServiceUnavailableException,
            RemoteServiceTimeoutException {
        return productImageService.attach(merchantStore, id, body);
    }

    /**
     * Replaces the whole gallery — order is the list order, and the item flagged default wins. This is also how
     * the default image is changed; the old {@code PATCH ?order=} could only renumber.
     */
    @PutMapping("/private/product/{id}/images")
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public List<ReadableImage> replace(@PathVariable Long id,
                                       @RequestBody @Valid List<PersistableProductImage> body,
                                       StoreMerchantId merchantStore)
            throws ProductNotFoundException, ProductImageAssetUnknownException, RemoteServiceUnavailableException,
            RemoteServiceTimeoutException {
        return productImageService.replace(merchantStore, id, body);
    }

    /**
     * Detaches an image. The asset stays in the media library, where it may still be used elsewhere.
     */
    @DeleteMapping("/private/product/{id}/image/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Parameter(name = "store", schema = @Schema(type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @PreAuthorize(MANAGE)
    public void delete(@PathVariable Long id, @PathVariable Long imageId, StoreMerchantId merchantStore)
            throws ProductImageNotFoundException {
        productImageService.delete(merchantStore, id, imageId);
    }
}
