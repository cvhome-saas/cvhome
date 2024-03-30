package com.asrevo.cvhome.store.controller.v1.product;

import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.catalog.product.review.ProductReviewService;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReview;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductReview;
import com.asrevo.cvhome.store.core.model.catalog.product.ReadableProductReview;
import com.asrevo.cvhome.store.service.facade.product.ProductCommonFacade;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1")
public class ProductReviewApi {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProductReviewApi.class);
    @Autowired
    private ProductCommonFacade productCommonFacade;
    @Autowired
    private ProductService productService;
    @Autowired
    private ProductReviewService productReviewService;

    @RequestMapping(
            value = {
                    "/private/products/{id}/reviews",
                    "/auth/products/{id}/reviews",
                    "/auth/products/{id}/reviews",
                    "/auth/products/{id}/reviews"
            },
            method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public PersistableProductReview create(
            @PathVariable final Long id,
            @Valid @RequestBody PersistableProductReview review,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // rating already exist
            ProductReview prodReview =
                    productReviewService.getByProductAndCustomer(
                            review.getProductId(), review.getCustomerId());
            if (prodReview != null) {
                response.sendError(500, "A review already exist for this customer and product");
                return null;
            }

            // rating maximum 5
            if (review.getRating() > Constants.MAX_REVIEW_RATING_SCORE) {
                response.sendError(503, "Maximum rating score is " + Constants.MAX_REVIEW_RATING_SCORE);
                return null;
            }

            review.setProductId(id);

            productCommonFacade.saveOrUpdateReview(review, merchantStore, language);

            return review;

        } catch (Exception e) {
            LOGGER.error("Error while saving product review", e);
            try {
                response.sendError(503, "Error while saving product review" + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    @RequestMapping(value = "/product/{id}/reviews", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public List<ReadableProductReview> getAll(
            @PathVariable final Long id,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response) {

        try {
            // product exist
            Product product = productService.getById(id);

            if (product == null) {
                response.sendError(404, "Product id " + id + " does not exists");
                return null;
            }

            List<ReadableProductReview> reviews =
                    productCommonFacade.getProductReviews(product, merchantStore, language);

            return reviews;

        } catch (Exception e) {
            LOGGER.error("Error while getting product reviews", e);
            try {
                response.sendError(503, "Error while getting product reviews" + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    @RequestMapping(
            value = {
                    "/private/products/{id}/reviews/{reviewid}",
                    "/auth/products/{id}/reviews/{reviewid}"
            },
            method = RequestMethod.PUT)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public PersistableProductReview update(
            @PathVariable final Long id,
            @PathVariable final Long reviewId,
            @Valid @RequestBody PersistableProductReview review,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            ProductReview prodReview = productReviewService.getById(reviewId);
            if (prodReview == null) {
                response.sendError(404, "Product review with id " + reviewId + " does not exist");
                return null;
            }

            if (prodReview.getCustomer().getId().longValue() != review.getCustomerId().longValue()) {
                response.sendError(404, "Product review with id " + reviewId + " does not exist");
                return null;
            }

            // rating maximum 5
            if (review.getRating() > Constants.MAX_REVIEW_RATING_SCORE) {
                response.sendError(503, "Maximum rating score is " + Constants.MAX_REVIEW_RATING_SCORE);
                return null;
            }

            review.setProductId(id);

            productCommonFacade.saveOrUpdateReview(review, merchantStore, language);

            return review;

        } catch (Exception e) {
            LOGGER.error("Error while saving product review", e);
            try {
                response.sendError(503, "Error while saving product review" + e.getMessage());
            } catch (Exception ignore) {
            }

            return null;
        }
    }

    @RequestMapping(
            value = {
                    "/private/products/{id}/reviews/{reviewid}",
                    "/auth/products/{id}/reviews/{reviewid}"
            },
            method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = Constants.DEFAULT_STORE)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public void delete(
            @PathVariable final Long id,
            @PathVariable final Long reviewId,
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language,
            HttpServletResponse response) {

        try {
            ProductReview prodReview = productReviewService.getById(reviewId);
            if (prodReview == null) {
                response.sendError(404, "Product review with id " + reviewId + " does not exist");
                return;
            }

            if (prodReview.getProduct().getId().longValue() != id.longValue()) {
                response.sendError(404, "Product review with id " + reviewId + " does not exist");
                return;
            }

            productCommonFacade.deleteReview(prodReview, merchantStore, language);

        } catch (Exception e) {
            LOGGER.error("Error while deleting product review", e);
            try {
                response.sendError(503, "Error while deleting product review" + e.getMessage());
            } catch (Exception ignore) {
            }

            return;
        }
    }
}
