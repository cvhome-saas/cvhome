package com.asrevo.cvhome.catalog.api.v1.product;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.image.ProductImage;
import com.asrevo.cvhome.catalog.model.product.ReadableImage;
import com.asrevo.cvhome.catalog.service.mapper.catalog.ReadableProductImageMapper;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.catalog.services.product.image.ProductImageService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.commons.domain.LanguageCode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.extern.slf4j.Slf4j;

import static com.asrevo.cvhome.commons.utils.DefaultStoresConstants.DEFAULT_ORG1_STORE1_STR;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Product images management. Add, remove and set the order of product images.")
@Slf4j
public class ProductImageApi {

    private static final String ERR_DELETING_IMAGE = "Error while deleting ProductImage";
    private static final String ERR_IMAGE_NOT_FOUND = "Product image [%s] not found for product id [%s] and merchant [%s]";
    private static final String ERR_IMAGES_NOT_FOUND = "Product images not found for product id [%s] and merchant [%s]";
    private static final String ERR_PRODUCT_IMAGE = "ProductImage [%s]";

    private final ProductImageService productImageService;

    private final ProductService productService;

    private final ReadableProductImageMapper readableProductImageMapper;

    public ProductImageApi(ProductImageService productImageService, ProductService productService,
                           ReadableProductImageMapper readableProductImageMapper) {
        this.productImageService = productImageService;
        this.productService = productService;
        this.readableProductImageMapper = readableProductImageMapper;
    }

    /**
     * To be used with MultipartFile
     */
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/private/product/{id}/image",
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})

    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void uploadImage(@PathVariable Long id, @RequestParam(value = "file") MultipartFile[] files,
                            @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
                            @RequestParam(value = "defaultImage", required = false, defaultValue = "false") boolean defaultImage,
                            StoreMerchantId merchantStore, LanguageCode language) {

        try {

            // get the product
            Product product = productService.getById(id);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found");
            }

            // security validation
            // product belongs to merchant store
            if (!Objects.equals(product.getStore(), merchantStore)) {
                throw new UnauthorizedException("Resource not authorized for this merchant");
            }

            boolean hasDefaultImage = false;
            Set<ProductImage> images = product.getImages();

            if (!defaultImage && !CollectionUtils.isEmpty(images)) {
                for (ProductImage image : images) {
                    if (image.isDefaultImage()) {
                        hasDefaultImage = true;
                        break;
                    }
                }
            }

            List<ProductImage> contentImagesList = new ArrayList<>();
            int sortOrder = position;
            for (MultipartFile multipartFile : files) {
                if (!multipartFile.isEmpty()) {
                    ProductImage productImage = new ProductImage();
                    productImage.setImage(multipartFile.getInputStream());
                    productImage.setProductImage(multipartFile.getOriginalFilename());
                    productImage.setProduct(product);

                    if (!hasDefaultImage) {
                        productImage.setDefaultImage(true);
                        hasDefaultImage = true;
                    }
                    productImage.setSortOrder(sortOrder);
                    position++;
                    contentImagesList.add(productImage);
                }
            }

            if (CollectionUtils.isNotEmpty(contentImagesList)) {
                productImageService.addProductImages(product, contentImagesList);
            }

        } catch (Exception e) {
            log.error("Error while creating ProductImage", e);
            throw new ServiceRuntimeException("Error while creating image");
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @DeleteMapping(value = {"/private/product/{id}/image/{imageId}"})
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void deleteImage(@PathVariable Long id, @PathVariable Long imageId, StoreMerchantId merchantStore,
                            LanguageCode language) {

        Optional<ProductImage> productImage = productImageService.getProductImage(imageId, id, merchantStore);

        if (productImage.isPresent()) {
            try {
                productImageService.delete(productImage.get());
            } catch (ServiceException e) {
                log.error(ERR_DELETING_IMAGE, e);
                throw new ServiceRuntimeException(String.format(ERR_PRODUCT_IMAGE, imageId) + " cannot be deleted", e);
            }
        } else {
            throw new ResourceNotFoundException(String.format(ERR_IMAGE_NOT_FOUND, imageId, id, merchantStore));
        }
    }

    /**
     * Get product images
     */
    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/product/{productId}/images"})
    @Operation(method = "GET", description = "Get images for a given product")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "List of ProductImage found")})


    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    public List<ReadableImage> images(@PathVariable Long productId, StoreMerchantId merchantStore,
                                      LanguageCode language) {

        Product p = productService.getById(productId);

        if (p == null) {
            throw new ResourceNotFoundException(String.format(ERR_IMAGES_NOT_FOUND, productId, merchantStore));
        }

        if (!p.getStore().equals(merchantStore)) {
            throw new ResourceNotFoundException(String.format(ERR_IMAGES_NOT_FOUND, productId, merchantStore));
        }

        List<ReadableImage> target = new ArrayList<>();

        Set<ProductImage> images = p.getImages();
        if (images != null && !images.isEmpty()) {

            target = images.stream()
                    .map(i -> image(i, merchantStore, language))
                    .sorted(Comparator.comparingInt(ReadableImage::getOrder))
                    .toList();
        }

        return target;
    }

    private ReadableImage image(ProductImage image, StoreMerchantId store, LanguageCode language) {
        return readableProductImageMapper.convert(image, store, language);
    }

    /**
     * Patch image (change position)
     */
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/private/product/{id}/image/{imageId}")
    @Parameter(name = "store",
            schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1_STR))
    @Parameter(name = "lang",
            schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.CATALOG.*')")
    public void imageDetails(@PathVariable Long id, @PathVariable Long imageId,
                             @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
                             StoreMerchantId merchantStore, LanguageCode language) {

        try {

            Product p = productService.getById(id);

            if (p == null) {
                throw new ResourceNotFoundException(String.format(ERR_IMAGE_NOT_FOUND, imageId, id, merchantStore));
            }

            if (!p.getStore().equals(merchantStore)) {
                throw new ResourceNotFoundException(String.format(ERR_IMAGE_NOT_FOUND, imageId, id, merchantStore));
            }

            Optional<ProductImage> productImage = productImageService.getProductImage(imageId, id, merchantStore);

            if (productImage.isPresent()) {
                productImage.get().setSortOrder(position);
                productImageService.updateProductImage(p, productImage.get());
            } else {
                throw new ResourceNotFoundException(String.format(ERR_IMAGE_NOT_FOUND, imageId, id, merchantStore));
            }

        } catch (Exception e) {
            log.error(ERR_DELETING_IMAGE, e);
            throw new ServiceRuntimeException(String.format(ERR_PRODUCT_IMAGE, imageId) + " cannot be edited");
        }
    }

}
