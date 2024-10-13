package com.asrevo.cvhome.store.core.services.customer.review;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReview;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

public interface CustomerReviewService extends SalesManagerEntityService<Long, CustomerReview> {

    /**
     * All reviews created by a given customer
     *
     */
    List<CustomerReview> getByCustomer(Customer customer);

    /**
     * All reviews received by a given customer
     *
     */
    List<CustomerReview> getByReviewedCustomer(Customer customer);

    /**
     * Get a review made by a customer to another customer
     *
     */
    CustomerReview getByReviewerAndReviewed(Long reviewer, Long reviewed);
}
