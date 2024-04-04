package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.controller.exception.UnauthorizedException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.category.Category;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductCriteria;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.LightPersistableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;
import com.asrevo.cvhome.store.core.model.catalog.product.product.PersistableProduct;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.services.catalog.category.CategoryService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.service.facade.product.ProductCommonFacade;
import com.asrevo.cvhome.store.service.facade.product.ProductFacade;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * API to create, read, update and delete a Product API.
 *
 * @author Carl Samson
 */
@Controller
@RequestMapping("/api/v1")
@Tag(name =
        "Product definition resource (Create udtate and delete product definition. Serves api v1 and v2 with backward compatibility)")
@Slf4j
public class ProductApi {

    private final CategoryService categoryService;
    private final ProductService productService;
    private final ProductFacade productFacade;
    private final ProductCommonFacade productCommonFacade;
    private final ImageFilePath imageUtils;

    public ProductApi(CategoryService categoryService, ProductService productService, ProductFacade productFacade, ProductCommonFacade productCommonFacade, @Qualifier("img") ImageFilePath imageUtils) {
        this.categoryService = categoryService;
        this.productService = productService;
        this.productFacade = productFacade;
        this.productCommonFacade = productCommonFacade;
        this.imageUtils = imageUtils;
    }

    /**
     * Create product
     *
     * @param product
     * @param merchantStore
     * @param language
     * @return Entity
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product", "/auth/products"}, // private
            // for
            // adding
            // products
            method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody Entity create(@Valid @RequestBody PersistableProduct product,
                                       @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        Long id = productCommonFacade.saveProduct(merchantStore, product, language);
        Entity returnEntity = new Entity();
        returnEntity.setId(id);
        return returnEntity;

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}", "/auth/product/{id}"}, method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "PUT", description = "Update product", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = PersistableProduct.class)))
    )
    public void update(@PathVariable Long id,
                       @Valid @RequestBody PersistableProduct product, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
                       HttpServletRequest request, HttpServletResponse response) {

        try {
            // Make sure we have consistency in this request
            if (!id.equals(product.getId())) {
                response.sendError(400, "Error url id does not match object id");
            }

            productCommonFacade.saveProduct(merchantStore, product,
                    merchantStore.getDefaultLanguage());
        } catch (Exception e) {
            log.error("Error while updating product", e);
            try {
                response.sendError(503, "Error while updating product " + e.getMessage());
            } catch (Exception ignore) {
            }

        }
    }

    /**
     * updates price quantity
     **/
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/private/product/{id}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "PATCH", description = "Update product inventory", summary = "Updates product inventory",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = Void.class)))
    )
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(
            @PathVariable Long id,
            @Valid @RequestBody
            LightPersistableProduct product,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        productCommonFacade.update(id, product, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}", "/auth/product/{id}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(@PathVariable Long id, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        productCommonFacade.deleteProduct(id, merchantStore);
    }

    /**
     * List products
     * Filtering product lists based on product option and option value ?category=1
     * &manufacturer=2 &type=... &lang=en|fr NOT REQUIRED, will use request language
     * &start=0 NOT REQUIRED, can be used for pagination &count=10 NOT REQUIRED, can
     * be used to limit item count
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/products", method = RequestMethod.GET)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductList list(
            @RequestParam(value = "lang", required = false) String lang,
            @RequestParam(value = "category", required = false) Long category,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "sku", required = false) String sku,
            @RequestParam(value = "manufacturer", required = false) Long manufacturer,
            @RequestParam(value = "optionValues", required = false) List<Long> optionValueIds,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "owner", required = false) Long owner,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page, // current
            @RequestParam(value = "origin", required = false, defaultValue = ProductCriteria.ORIGIN_SHOP) String origin,
            // page
            // 0
            // ..
            // n
            // allowing
            // navigation
            @RequestParam(value = "count", required = false, defaultValue = "100") Integer count, // count
            @RequestParam(value = "slug", required = false) String slug, // category slug
            @RequestParam(value = "available", required = false) Boolean available,
            // per
            // page
            @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language, HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        ProductCriteria criteria = new ProductCriteria();

        criteria.setOrigin(origin);

        // do not use legacy pagination anymore
        if (lang != null) {
            criteria.setLanguage(lang);
        } else {
            criteria.setLanguage(language.getCode());
        }
        if (!StringUtils.isBlank(status)) {
            criteria.setStatus(status);
        }
        // Start Category handling
        List<Long> categoryIds = new ArrayList<Long>();
        if (slug != null) {
            Category categoryBySlug = categoryService.getBySeUrl(merchantStore, slug, language);
            categoryIds.add(categoryBySlug.getId());
        }
        if (category != null) {
            categoryIds.add(category);
        }
        if (!categoryIds.isEmpty()) {
            criteria.setCategoryIds(categoryIds);
        }
        // End Category handling

        if (available != null && available) {
            criteria.setAvailable(available);
        }

        if (manufacturer != null) {
            criteria.setManufacturerId(manufacturer);
        }

        if (CollectionUtils.isNotEmpty(optionValueIds)) {
            criteria.setOptionValueIds(optionValueIds);
        }

        if (owner != null) {
            criteria.setOwnerId(owner);
        }

        if (page != null) {
            criteria.setStartPage(page);
        }

        if (count != null) {
            criteria.setMaxCount(count);
        }

        if (!StringUtils.isBlank(name)) {
            criteria.setProductName(name);
        }

        if (!StringUtils.isBlank(sku)) {
            criteria.setCode(sku);
        }

        // TODO
        // RENTAL add filter by owner
        // REPOSITORY to use the new filters

        try {
            return productFacade.getProductListsByCriterias(merchantStore, language, criteria);

        } catch (Exception e) {

            log.error("Error while filtering products product", e);
            try {
                response.sendError(503, "Error while filtering products " + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    /**
     * API for getting a product
     * Removed in 3.2.5 in favor of /product/sku
     *
     * @param id
     * @param lang     ?lang=fr|en|...
     * @param response
     * @return ReadableProduct
     * @throws Exception
     *                   <p>
     *                   /api/product/123
     */
    /**
     @RequestMapping(value = {"/product/{id}","/products/{id}"}, method = RequestMethod.GET)
     @Operation(method = "GET", value = "Get a product by id", notes = "For administration and shop purpose. Specifying ?merchant is required otherwise it falls back to DEFAULT")
     @ApiResponses(value = {
     @ApiResponse(code = 200, message = "Single product found", response = ReadableProduct.class) })
     @ResponseBody
     @Parameters({
     @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
     @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
     })
     public ReadableProduct get(@PathVariable final Long id, @RequestParam(value = "lang", required = false) String lang,
     @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language, HttpServletResponse response)
     throws Exception {
     ReadableProduct product = productCommonFacade.getProduct(merchantStore, id, language);

     if (product == null) {
     response.sendError(404, "Product not fount for id " + id);
     return null;
     }

     return product;
     }
     **/

    /**
     * Price calculation
     * @param id
     * @param variants
     * @param merchantStore
     * @param language
     * @return
     */
    /**
     @RequestMapping(value = "/product/{id}/price", method = RequestMethod.POST)
     @Operation(method = "POST", value = "Calculate product price with variants", notes = "Product price calculation from variants")
     @ApiResponses(value = {
     @ApiResponse(code = 200, message = "Price calculated", response = ReadableProductPrice.class) })
     @ResponseBody
     @Parameters({
     @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
     @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
     })
     public ReadableProductPrice price(@PathVariable final Long id,
     @RequestBody ProductPriceRequest variants,
     @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

     return productFacade.getProductPrice(id, variants, merchantStore, language);

     }
     **/

    /**
     * API for getting a product
     *
     * @param friendlyUrl
     * @param lang        ?lang=fr|en
     * @param response
     * @return ReadableProduct
     * @throws Exception <p>
     *                   /api/product/123
     */
    @RequestMapping(value = {"/product/{friendlyUrl}",
            "/product/friendly/{friendlyUrl}"}, method = RequestMethod.GET)
    @Operation(method = "GET", description = "Get a product by friendlyUrl (slug)", summary = "For administration and shop purpose. Specifying ?merchant is "
            + "required otherwise it falls back to DEFAULT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Single product found")})
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProduct getByfriendlyUrl(@PathVariable final String friendlyUrl,
                                            @RequestParam(value = "lang", required = false) String lang, @Parameter(hidden = true) MerchantStore merchantStore,
                                            @Parameter(hidden = true) Language language, HttpServletResponse response) throws Exception {
        ReadableProduct product = productFacade.getProductBySeUrl(merchantStore, friendlyUrl, language);

        if (product == null) {
            response.sendError(404, "Product not fount for id " + friendlyUrl);
            return null;
        }

        return product;
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Check if product code already exists", summary = "",
            responses = @ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class)))
    )
    public ResponseEntity<EntityExists> exists(@RequestParam(value = "code") String code,
                                               @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        boolean exists = productCommonFacade.exists(code, merchantStore);
        return new ResponseEntity<EntityExists>(new EntityExists(exists), HttpStatus.OK);

    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/{productId}/category/{categoryId}"}, method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void addProductToCategory(@PathVariable Long productId,
                                     @PathVariable Long categoryId, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language,
                                     HttpServletResponse response) throws Exception {

        try {
            // get the product
            Product product = productService.getById(productId);

            if (product == null) {
                throw new ResourceNotFoundException("Product id [" + productId + "] is not found");
            }

            if (product.getMerchantStore().getId().intValue() != merchantStore.getId().intValue()) {
                throw new UnauthorizedException(
                        "Product id [" + productId + "] does not belong to store [" + merchantStore.getCode() + "]");
            }

            Category category = categoryService.getById(categoryId);

            if (category == null) {
                throw new ResourceNotFoundException("Category id [" + categoryId + "] is not found");
            }

            if (category.getMerchantStore().getId().intValue() != merchantStore.getId().intValue()) {
                throw new UnauthorizedException(
                        "Category id [" + categoryId + "] does not belong to store [" + merchantStore.getCode() + "]");
            }

            productCommonFacade.addProductToCategory(category, product, language);

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{productId}/category/{categoryId}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void removeProductFromCategory(@PathVariable Long productId,
                                          @PathVariable Long categoryId, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        try {
            Product product = productService.getById(productId);

            if (product == null) {
                throw new ResourceNotFoundException("Product id [" + productId + "] is not found");
            }

            if (product.getMerchantStore().getId().intValue() != merchantStore.getId().intValue()) {
                throw new UnauthorizedException(
                        "Product id [" + productId + "] does not belong to store [" + merchantStore.getCode() + "]");
            }

            Category category = categoryService.getById(categoryId);

            if (category == null) {
                throw new ResourceNotFoundException("Category id [" + categoryId + "] is not found");
            }

            if (category.getMerchantStore().getId().intValue() != merchantStore.getId().intValue()) {
                throw new UnauthorizedException(
                        "Category id [" + categoryId + "] does not belong to store [" + merchantStore.getCode() + "]");
            }

            productCommonFacade.removeProductFromCategory(category, product, language);

        } catch (Exception e) {
            throw new ServiceRuntimeException(e);
        }
    }

    /**
     * Change product sort order
     *
     * @param id
     * @param position
     * @param merchantStore
     * @param language
     * @throws IOException
     */

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}", "/auth/product/{id}"}, method = RequestMethod.PATCH)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "POST", description = "Patch product sort order", summary = "Change product sortOrder")
    public void changeProductOrder(@PathVariable Long id,
                                   @RequestParam(value = "order", required = false, defaultValue = "0") Integer position,
                                   @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) throws IOException {

        try {

            Product p = productService.getById(id);

            if (p == null) {
                throw new ResourceNotFoundException(
                        "Product [" + id + "] not found for merchant [" + merchantStore.getCode() + "]");
            }

            if (p.getMerchantStore().getId() != merchantStore.getId()) {
                throw new ResourceNotFoundException(
                        "Product [" + id + "] not found for merchant [" + merchantStore.getCode() + "]");
            }

            /**
             * Change order
             */
            p.setSortOrder(position);

        } catch (Exception e) {
            log.error("Error while updating Product position", e);
            throw new ServiceRuntimeException("Product [" + id + "] cannot be edited");
        }
    }

}
