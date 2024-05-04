package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.image.ProductImage;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableImage;
import com.asrevo.cvhome.store.core.model.entity.NameEntity;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.image.ProductImageService;
import com.asrevo.cvhome.store.service.mapper.catalog.ReadableProductImageMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_STORE;

@Controller
@RequestMapping("/api/v1")
@Tag(name = "Product images management. Add, remove and set the order of product images.")
@Slf4j
public class ProductImageApi {

    private final ProductImageService productImageService;
    private final ProductService productService;
    private final ReadableProductImageMapper readableProductImageMapper;

    public ProductImageApi(ProductImageService productImageService, ProductService productService, ReadableProductImageMapper readableProductImageMapper) {
        this.productImageService = productImageService;
        this.productService = productService;
        this.readableProductImageMapper = readableProductImageMapper;
    }

    /**
     * To be used with MultipartFile
     *
     * @param id
     * @param uploadfiles
     * @param request
     * @param response
     * @throws Exception
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/{id}/image", "/auth/product/{id}/image"}, consumes = {
            MediaType.MULTIPART_FORM_DATA_VALUE}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void uploadImage(
            @PathVariable Long id,
            @RequestParam(value = "file", required = true) MultipartFile[] files,
            @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
            @RequestParam(value = "defaultImage", required = false, defaultValue = "false") boolean defaultImage,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) throws IOException {

        try {

            // get the product
            Product product = productService.getById(id);
            if (product == null) {
                throw new ResourceNotFoundException("Product not found");
            }

            // security validation
            // product belongs to merchant store
            if (product.getMerchantStore().getId().intValue() != merchantStore.getId().intValue()) {
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

            List<ProductImage> contentImagesList = new ArrayList<ProductImage>();
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
    @RequestMapping(value = {"/private/product/image/{id}",
            "/auth/product/images/{id}"}, method = RequestMethod.DELETE)
    public void deleteImage(@PathVariable Long id, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        try {
            ProductImage productImage = productImageService.getById(id);

            if (productImage != null) {
                productImageService.delete(productImage);
            } else {
                response.sendError(404, "No ProductImage found for ID : " + id);
            }

        } catch (Exception e) {
            log.error("Error while deleting ProductImage", e);
            try {
                response.sendError(503, "Error while deleting ProductImage " + e.getMessage());
            } catch (Exception ignore) {
            }
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}/image/{imageId}"}, method = RequestMethod.DELETE)
    public void deleteImage(@PathVariable Long id, @PathVariable Long imageId, @Valid NameEntity imageName,
                            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {


        Optional<ProductImage> productImage = productImageService.getProductImage(imageId, id, merchantStore);

        if (productImage.isPresent()) {
            try {
                productImageService.delete(productImage.get());
            } catch (ServiceException e) {
                log.error("Error while deleting ProductImage", e);
                throw new ServiceRuntimeException("ProductImage [" + imageId + "] cannot be deleted", e);

            }
        } else {
            throw new ResourceNotFoundException("Product image [" + imageId
                    + "] not found for product id [" + id + "] and merchant [" + merchantStore.getCode() + "]");
        }

    }


    /**
     * Get product images
     *
     * @param id
     * @param imageId
     * @param merchantStore
     * @param language
     * @return
     */

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/product/{productId}/images"}, method = RequestMethod.GET)
    @Operation(method = "GET", description = "Get images for a given product")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of ProductImage found")})
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableImage> images(
            @PathVariable Long productId,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {


        Product p = productService.getById(productId);

        if (p == null) {
            throw new ResourceNotFoundException("Product images not found for product id [" + productId
                    + "] and merchant [" + merchantStore.getCode() + "]");
        }

        if (p.getMerchantStore().getId() != merchantStore.getId()) {
            throw new ResourceNotFoundException("Product images not found for product id [" + productId
                    + "] and merchant [" + merchantStore.getCode() + "]");
        }

        List<ReadableImage> target = new ArrayList<>();

        Set<ProductImage> images = p.getImages();
        if (images != null && !images.isEmpty()) {


            target = images.stream().map(i -> image(i, merchantStore, language))
                    .sorted(Comparator.comparingInt(ReadableImage::getOrder))
                    .collect(Collectors.toList());


        }

        return target;

    }

    private ReadableImage image(ProductImage image, MerchantStore store, Language language) {
        return readableProductImageMapper.convert(image, store, language);
    }


    /**
     * Patch image (change position)
     *
     * @param id
     * @param files
     * @param position
     * @param merchantStore
     * @param language
     * @throws IOException
     */

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}/image/{imageId}",
            "/auth/product/{id}/image"}, method = RequestMethod.PATCH)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void imageDetails(@PathVariable Long id, @PathVariable Long imageId,
                             @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
                             @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) throws IOException {

        try {

            Product p = productService.getById(id);

            if (p == null) {
                throw new ResourceNotFoundException("Product image [" + imageId + "] not found for product id [" + id
                        + "] and merchant [" + merchantStore.getCode() + "]");
            }

            if (p.getMerchantStore().getId() != merchantStore.getId()) {
                throw new ResourceNotFoundException("Product image [" + imageId + "] not found for product id [" + id
                        + "] and merchant [" + merchantStore.getCode() + "]");
            }

            Optional<ProductImage> productImage = productImageService.getProductImage(imageId, id, merchantStore);

            if (productImage.isPresent()) {
                productImage.get().setSortOrder(position);
                productImageService.updateProductImage(p, productImage.get());
            } else {
                throw new ResourceNotFoundException("Product image [" + imageId + "] not found for product id [" + id
                        + "] and merchant [" + merchantStore.getCode() + "]");
            }


        } catch (Exception e) {
            log.error("Error while deleting ProductImage", e);
            throw new ServiceRuntimeException("ProductImage [" + imageId + "] cannot be edited");
        }
    }

}
