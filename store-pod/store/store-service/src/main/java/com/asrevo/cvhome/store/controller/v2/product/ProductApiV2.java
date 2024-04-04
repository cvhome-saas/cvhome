package com.asrevo.cvhome.store.controller.v2.product;

import com.asrevo.cvhome.commons.annotation.SecuredResource;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.ProductCriteria;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.category.ReadableCategory;
import com.asrevo.cvhome.store.core.model.catalog.product.LightPersistableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductList;
import com.asrevo.cvhome.store.core.model.catalog.product.product.PersistableProduct;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.PersistableProductDefinition;
import com.asrevo.cvhome.store.core.model.catalog.product.product.definition.ReadableProductDefinition;
import com.asrevo.cvhome.store.core.model.entity.Entity;
import com.asrevo.cvhome.store.service.facade.category.CategoryFacade;
import com.asrevo.cvhome.store.service.facade.product.ProductCommonFacade;
import com.asrevo.cvhome.store.service.facade.product.ProductDefinitionFacade;
import com.asrevo.cvhome.store.service.facade.product.ProductFacade;
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
import org.apache.commons.lang3.StringUtils;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * API to create, read, update and delete a Product API.
 *
 * @author Carl Samson
 */
@Controller
@RequestMapping("/api/v2")
@Tags(value = @Tag(name =
        "Product display and management resource (Product display and Management Api such as adding a product to category. Serves api v1 and v2 with backward compatibility)"))
@Slf4j
public class ProductApiV2 {


    @Autowired
    private ProductDefinitionFacade productDefinitionFacade;
    @Autowired
    private ProductFacade productFacadeV2;
    @Autowired
    private ProductCommonFacade productCommonFacade;
    @Autowired
    private CategoryFacade categoryFacade;

    /**
     * Create product inventory with variants, quantity and prices
     *
     * @param product
     * @param merchantStore
     * @param language
     * @return
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value = {"/private/product/inventory"},
            method = RequestMethod.POST)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody Entity create(
            @Valid @RequestBody PersistableProduct product,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        Long id = productCommonFacade.saveProduct(merchantStore, product, language);
        Entity returnEntity = new Entity();
        returnEntity.setId(id);
        return returnEntity;

    }


    /**
     * ------------ V2
     * <p>
     * --- product definition
     */

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = {"/private/product"})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody Entity createV2(@Valid @RequestBody PersistableProductDefinition product,
                                         @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        // make sure product id is null
        product.setId(null);
        Long id = productDefinitionFacade.saveProductDefinition(merchantStore, product, language);
        Entity returnEntity = new Entity();
        returnEntity.setId(id);
        return returnEntity;

    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping(value = {"/private/product/{id}"})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void updateV2(@PathVariable Long id,
                         @Valid @RequestBody PersistableProductDefinition product,
                         @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        productDefinitionFacade.update(id, product, merchantStore, language);

    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping(value = {"/private/product/{id}"})
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public @ResponseBody ReadableProductDefinition getV2(
            @PathVariable Long id,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {

        ReadableProductDefinition def = productDefinitionFacade.getProduct(merchantStore, id, language);
        return def;

    }

    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(value = {"/private/product/{id}"}, method = RequestMethod.DELETE)
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void deleteV2(@PathVariable Long id, @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore, @Parameter(hidden = true) Language language) {

        productCommonFacade.deleteProduct(id, merchantStore);
    }

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
    @RequestMapping(value = {"/product/name/{friendlyUrl}",
            "/product/friendly/{friendlyUrl}"}, method = RequestMethod.GET)
    @Operation(method = "GET", description = "Get a product by friendlyUrl (slug) version 2", summary = "For shop purpose. Specifying ?merchant is "
            + "required otherwise it falls back to DEFAULT")
    @ApiResponse(responseCode = "200", description = "Single product found", content = @Content(schema = @Schema(implementation = ReadableProduct.class)))
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProduct getByfriendlyUrl(
            @PathVariable final String friendlyUrl,
            @RequestParam(value = "lang", required = false) String lang, @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language, HttpServletResponse response) throws Exception {

        ReadableProduct product = productFacadeV2.getProductBySeUrl(merchantStore, friendlyUrl, language);

        if (product == null) {
            response.sendError(404, "Product not fount for id " + friendlyUrl);
            return null;
        }

        return product;
    }


    /**
     * List products by category
     * count and page are supported. Default values are set when not specified
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/products/category/{friendlyUrl}", method = RequestMethod.GET)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProductList list(
            @RequestParam(value = "lang", required = false) String lang,
            @PathVariable String friendlyUrl,
            @RequestParam(value = "page", required = false, defaultValue = "0") Integer page, // count
            @RequestParam(value = "count", required = false, defaultValue = "25") Integer count, // count
            @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {


        try {
            ReadableCategory category = categoryFacade.getCategoryByFriendlyUrl(merchantStore, friendlyUrl, language);
            ProductCriteria criterias = new ProductCriteria();

            List<Long> listOfIds = new ArrayList<Long>();
            listOfIds.add(category.getId());


            criterias.setCategoryIds(listOfIds);

            criterias.setMaxCount(count);
            criterias.setLanguage(language.getCode());
            criterias.setStartPage(page);

            return productFacadeV2.getProductListsByCriterias(merchantStore, language, criterias);


        } catch (ResourceNotFoundException rnf) {
            throw rnf;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            log.error("Error while getting category by friendlyUrl", e);
            throw new ServiceRuntimeException(e);
        }

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
            ProductCriteria searchCriterias,

            // page
            // 0
            // ..
            // n
            // allowing
            // navigation
            @RequestParam(value = "count", required = false, defaultValue = "100") Integer count, // count
            // per
            // page
            @Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {


        if (!StringUtils.isBlank(searchCriterias.getSku())) {
            searchCriterias.setCode(searchCriterias.getSku());
        }

        if (!StringUtils.isBlank(searchCriterias.getName())) {
            searchCriterias.setProductName(searchCriterias.getName());
        }

        searchCriterias.setMaxCount(count);
        searchCriterias.setLanguage(language.getCode());

        try {
            return productFacadeV2.getProductListsByCriterias(merchantStore, language, searchCriterias);

        } catch (Exception e) {
            log.error("Error while filtering products product", e);
            throw new ServiceRuntimeException(e);

        }
    }

    /**
     * updates price quantity
     **/
    @ResponseStatus(HttpStatus.OK)
    @PatchMapping(value = "/private/product/{sku}", produces = {APPLICATION_JSON_VALUE})
    @Operation(method = "PATCH", description = "Update product inventory", summary = "Updates product inventory", responses = @ApiResponse(content = @Content(mediaType = "application/json", schema = @Schema(implementation = Void.class))))
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void update(
            @PathVariable String sku,
            @Valid @RequestBody
            LightPersistableProduct product,
            @Parameter(hidden = true) @SecuredResource MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        productCommonFacade.update(sku, product, merchantStore, language);

    }


    /**
     * API for getting a product using sku in v2
     *
     * @param id
     * @param lang     ?lang=fr|en|...
     * @param response
     * @return ReadableProduct
     * @throws Exception <p>
     *                   /api/products/123
     */
    @RequestMapping(value = "/product/{sku}", method = RequestMethod.GET)
    @Operation(method = "GET", description = "Get a product by sku", summary = "For Shop purpose. Specifying ?merchant is required otherwise it falls back to DEFAULT")
    @ApiResponse(responseCode = "200", description = "Single product found", content = @Content(schema = @Schema(implementation = ReadableProduct.class)))
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public ReadableProduct get(@PathVariable final String sku,
                               @RequestParam(value = "lang", required = false) String lang,
                               @Parameter(hidden = true) MerchantStore merchantStore,
                               @Parameter(hidden = true) Language language) {
        ReadableProduct product = productFacadeV2.getProductByCode(merchantStore, sku, language);


        return product;
    }
}
