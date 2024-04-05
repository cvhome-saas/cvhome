package com.asrevo.cvhome.store.service.populator.catalog;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReview;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReviewDescription;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.catalog.product.PersistableProductReview;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.customer.CustomerService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import com.asrevo.cvhome.store.utils.DateUtil;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;


@Setter
@Getter
public class PersistableProductReviewPopulator extends
        AbstractDataPopulator<PersistableProductReview, ProductReview> {


    private CustomerService customerService;


    private ProductService productService;


    private LanguageService languageService;


    @Override
    public ProductReview populate(PersistableProductReview source,
                                  ProductReview target, MerchantStore store, Language language)
            throws ConversionException {


        Assert.notNull(customerService, "customerService cannot be null");
        Assert.notNull(productService, "productService cannot be null");
        Assert.notNull(languageService, "languageService cannot be null");
        Assert.notNull(source.getRating(), "Rating cannot bot be null");

        try {

            if (target == null) {
                target = new ProductReview();
            }

            Customer customer = customerService.getById(source.getCustomerId());

            //check if customer belongs to store
            if (customer == null || customer.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                throw new ConversionException("Invalid customer id for the given store");
            }

            if (source.getDate() == null) {
                String date = DateUtil.formatDate(new Date());
                source.setDate(date);
            }
            target.setReviewDate(DateUtil.getDate(source.getDate()));
            target.setCustomer(customer);
            target.setReviewRating(source.getRating());

            Product product = productService.getById(source.getProductId());

            //check if product belongs to store
            if (product == null || product.getMerchantStore().getId().intValue() != store.getId().intValue()) {
                throw new ConversionException("Invalid product id for the given store");
            }

            target.setProduct(product);

            Language lang = languageService.getByCode(language.getCode());
            if (lang == null) {
                throw new ConversionException("Invalid language code, use iso codes (en, fr ...)");
            }

            ProductReviewDescription description = new ProductReviewDescription();
            description.setDescription(source.getDescription());
            description.setLanguage(lang);
            description.setName("-");
            description.setProductReview(target);

            Set<ProductReviewDescription> descriptions = new HashSet<ProductReviewDescription>();
            descriptions.add(description);

            target.setDescriptions(descriptions);


            return target;

        } catch (Exception e) {
            throw new ConversionException("Cannot populate ProductReview", e);
        }

    }

    @Override
    protected ProductReview createTarget() {
        return null;
    }


}
