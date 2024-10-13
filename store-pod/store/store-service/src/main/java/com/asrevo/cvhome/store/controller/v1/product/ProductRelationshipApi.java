package com.asrevo.cvhome.store.controller.v1.product;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProduct;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.review.ProductReviewService;
import com.asrevo.cvhome.store.service.facade.product.ProductFacade;
import com.asrevo.cvhome.store.service.facade.store.StoreFacade;
import com.asrevo.cvhome.store.utils.LanguageUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/v1")
@Slf4j
public class ProductRelationshipApi {

    private final ProductFacade productFacade;
    private final ProductService productService;

    public ProductRelationshipApi(
            ProductFacade productFacade,
            StoreFacade storeFacade,
            LanguageUtils languageUtils,
            ProductService productService,
            ProductReviewService productReviewService) {
        this.productFacade = productFacade;
        this.productService = productService;
    }

    /*	@RequestMapping( value={"/private/products/{id}/related","/auth/products/{id}/related"}, method=RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public PersistableProductReview create(@PathVariable final Long id, @Valid @RequestBody PersistableProductReview review, HttpServletRequest request, HttpServletResponse response) throws Exception {


    	try {

    		MerchantStore merchantStore = storeFacade.getByCode(request);
    		Language language = languageUtils.getRESTLanguage(request, merchantStore);

    		//rating already exist
    		ProductReview prodReview = productReviewService.getByProductAndCustomer(review.getProductId(), review.getCustomerId());
    		if(prodReview!=null) {
    			response.sendError(500, "A review already exist for this customer and product");
    			return null;
    		}

    		//rating maximum 5
    		if(review.getRating()>Constants.MAX_REVIEW_RATING_SCORE) {
    			response.sendError(503, "Maximum rating score is " + Constants.MAX_REVIEW_RATING_SCORE);
    			return null;
    		}

    		review.setProductId(id);



    		productFacade.saveOrUpdateReview(review, merchantStore, language);

    		return review;

    	} catch (Exception e) {
    		LOGGER.error("Error while saving product review",e);
    		try {
    			response.sendError(503, "Error while saving product review" + e.getMessage());
    		} catch (Exception ignore) {
    		}

    		return null;
    	}
    }*/

    @RequestMapping(value = "/product/{id}/related", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @Operation(
            method = "GET",
            description =
                    "Get product related items. This is used for doing cross-sell and up-sell"
                            + " functionality on a product details page",
            responses =
                    @ApiResponse(content = @Content(schema = @Schema(implementation = List.class))))
    @ResponseBody
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
    public List<ReadableProduct> getAll(
            @PathVariable final Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response)
            throws Exception {

        try {
            // product exist
            Product product = productService.getById(id);

            if (product == null) {
                response.sendError(404, "Product id " + id + " does not exists");
                return null;
            }

            List<ReadableProduct> relatedItems =
                    productFacade.relatedItems(merchantStore, product, language);

            return relatedItems;

        } catch (Exception e) {
            log.error("Error while getting product reviews", e);
            try {
                response.sendError(503, "Error while getting product reviews" + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    /*
    @RequestMapping( value={"/private/products/{id}/reviews/{reviewid}","/auth/products/{id}/reviews/{reviewid}"}, method=RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public PersistableProductReview update(@PathVariable final Long id, @PathVariable final Long reviewId, @Valid @RequestBody PersistableProductReview review, HttpServletRequest request, HttpServletResponse response) throws Exception {


    	try {

    		MerchantStore merchantStore = storeFacade.getByCode(request);
    		Language language = languageUtils.getRESTLanguage(request, merchantStore);

    		ProductReview prodReview = productReviewService.getById(reviewId);
    		if(prodReview==null) {
    			response.sendError(404, "Product review with id " + reviewId + " does not exist");
    			return null;
    		}

    		if(prodReview.getCustomer().getId().longValue() != review.getCustomerId().longValue()) {
    			response.sendError(404, "Product review with id " + reviewId + " does not exist");
    			return null;
    		}

    		//rating maximum 5
    		if(review.getRating()>Constants.MAX_REVIEW_RATING_SCORE) {
    			response.sendError(503, "Maximum rating score is " + Constants.MAX_REVIEW_RATING_SCORE);
    			return null;
    		}

    		review.setProductId(id);


    		productFacade.saveOrUpdateReview(review, merchantStore, language);

    		return review;

    	} catch (Exception e) {
    		LOGGER.error("Error while saving product review",e);
    		try {
    			response.sendError(503, "Error while saving product review" + e.getMessage());
    		} catch (Exception ignore) {
    		}

    		return null;
    	}
    }

    @RequestMapping( value={"/private/products/{id}/reviews/{reviewid}","/auth/products/{id}/reviews/{reviewid}"}, method=RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void delete(@PathVariable final Long id, @PathVariable final Long reviewId, HttpServletRequest request, HttpServletResponse response) throws Exception {


    	try {

    		MerchantStore merchantStore = storeFacade.getByCode(request);
    		Language language = languageUtils.getRESTLanguage(request, merchantStore);

    		ProductReview prodReview = productReviewService.getById(reviewId);
    		if(prodReview==null) {
    			response.sendError(404, "Product review with id " + reviewId + " does not exist");
    			return;
    		}

    		if(prodReview.getProduct().getId().longValue() != id.longValue()) {
    			response.sendError(404, "Product review with id " + reviewId + " does not exist");
    			return;
    		}


    		productFacade.deleteReview(prodReview, merchantStore, language);



    	} catch (Exception e) {
    		LOGGER.error("Error while deleting product review",e);
    		try {
    			response.sendError(503, "Error while deleting product review" + e.getMessage());
    		} catch (Exception ignore) {
    		}

    		return;
    	}
    }*/

}
