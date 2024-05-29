package com.asrevo.cvhome.store.core.services.catalog.product.review;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReview;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;

public interface ProductReviewService extends
        SalesManagerEntityService<Long, ProductReview> {


    List<ProductReview> getByCustomer(Customer customer);

    List<ProductReview> getByProduct(Product product);

    List<ProductReview> getByProduct(Product product, Language language);

    ProductReview getByProductAndCustomer(Long productId, Long customerId);

    /**
     */
    List<ProductReview> getByProductNoCustomers(Product product);


}
