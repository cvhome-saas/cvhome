package com.asrevo.cvhome.checkout.repositories.customer.review;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.checkout.entity.customer.review.CustomerReview;

public interface CustomerReviewRepository extends JpaRepository<CustomerReview, Long> {

}
