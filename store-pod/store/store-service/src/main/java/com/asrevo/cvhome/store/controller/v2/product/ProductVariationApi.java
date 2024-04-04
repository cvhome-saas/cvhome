package com.asrevo.cvhome.store.controller.v2.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.attribute.ProductAttribute;
import com.asrevo.cvhome.store.core.entity.catalog.product.price.FinalPrice;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductPrice;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableProductVariantValue;
import com.asrevo.cvhome.store.core.model.catalog.product.attribute.ReadableSelectedProductVariant;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.PersistableProductVariation;
import com.asrevo.cvhome.store.core.model.catalog.product.variation.ReadableProductVariation;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import com.asrevo.cvhome.store.core.model.entity.EntityExists;
import com.asrevo.cvhome.store.core.model.entity.ReadableEntityList;
import com.asrevo.cvhome.store.core.services.catalog.pricing.PricingService;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.service.facade.category.CategoryFacade;
import com.asrevo.cvhome.store.service.facade.product.ProductVariationFacade;
import com.asrevo.cvhome.store.service.populator.catalog.ReadableFinalPricePopulator;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.tags.Tags;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * API to manage product variant
 * <p>
 * The flow is the following
 * <p>
 * - create a product definition
 * - create a product variant
 *
 * @author Carl Samson
 */
@Controller
@RequestMapping("/api/v2")
@Tags(value = @Tag(name = "Product variation resource (Product variant Api)"))
@Slf4j
public class ProductVariationApi {


    @Autowired
    private PricingService pricingService;
    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryFacade categoryFacade;
    @Autowired
    private ProductVariationFacade productVariationFacade;
    @Autowired
    @Qualifier("img")
    private ImageFilePath imageUtils;

    /**
     * Calculates the price based on selected options if any
     *
     * @param id
     * @param options
     * @param merchantStore
     * @param language
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/product/{id}/variation", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "POST",
            description = "Get product price variation based on selected product",
            summary = "",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReadableProductPrice.class))))
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductPrice calculateVariant(
            @PathVariable final Long id,
            @RequestBody ReadableSelectedProductVariant options,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response)
            throws Exception {

        Product product = productService.getById(id);

        if (product == null) {
            response.sendError(404, "Product not fount for id " + id);
            return null;
        }

        List<ReadableProductVariantValue> ids = options.getOptions();

        if (CollectionUtils.isEmpty(ids)) {
            return null;
        }

        List<ReadableProductVariantValue> variants = options.getOptions();
        List<ProductAttribute> attributes = new ArrayList<ProductAttribute>();

        Set<ProductAttribute> productAttributes = product.getAttributes();
        for (ProductAttribute attribute : productAttributes) {
            Long option = attribute.getProductOption().getId();
            Long optionValue = attribute.getProductOptionValue().getId();
            for (ReadableProductVariantValue v : variants) {
                if (v.getOption().longValue() == option.longValue()
                        && v.getValue().longValue() == optionValue.longValue()) {
                    attributes.add(attribute);
                }
            }

        }

        FinalPrice price = pricingService.calculateProductPrice(product, attributes);
        ReadableProductPrice readablePrice = new ReadableProductPrice();
        ReadableFinalPricePopulator populator = new ReadableFinalPricePopulator();
        populator.setPricingService(pricingService);
        populator.populate(price, readablePrice, merchantStore, language);
        return readablePrice;
    }


    @RequestMapping(value = "/category/{id}/variations", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "GET",
            description = "Get all variation for all items in a given category",
            summary = "",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = List.class))))
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableProductVariant> categoryVariantList(
            @PathVariable final Long id, //category id
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response)
            throws Exception {

        return categoryFacade.categoryProductVariants(id, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/variation"}, method = RequestMethod.POST)
    @Operation(
            method = "POST",
            description = "Creates a new product variant",
            summary = "",
            responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody Entity create(
            @Valid @RequestBody PersistableProductVariation variation,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        Long variantId = productVariationFacade.create(variation, merchantStore, language);
        return new Entity(variantId);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/variation/unique"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @Operation(method = "GET", description = "Check if option set code already exists", summary = "", responses = {@ApiResponse(content = @Content(schema = @Schema(implementation = EntityExists.class)))})
    public ResponseEntity<EntityExists> exists(
            @RequestParam(value = "code") String code,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        boolean isOptionExist = productVariationFacade.exists(code, merchantStore);
        return new ResponseEntity<EntityExists>(new EntityExists(isOptionExist), HttpStatus.OK);
    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/variation/{variationId}"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    @ResponseBody
    public ReadableProductVariation get(
            @PathVariable Long variationId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        return productVariationFacade.get(variationId, merchantStore, language);

    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/variation/{variationId}"}, method = RequestMethod.PUT)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(
            @Valid @RequestBody PersistableProductVariation variation,
            @PathVariable Long variationId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        variation.setId(variationId);
        productVariationFacade.update(variationId, variation, merchantStore, language);

    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/variation/{variationId}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(
            @PathVariable Long variationId,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        productVariationFacade.delete(variationId, merchantStore);

    }


    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/variations"}, method = RequestMethod.GET)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableEntityList<ReadableProductVariation> list(
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page,
            @RequestParam(value = "count", required = false, defaultValue = "10") Integer count) {

        return productVariationFacade.list(merchantStore, language, page, count);


    }

}
