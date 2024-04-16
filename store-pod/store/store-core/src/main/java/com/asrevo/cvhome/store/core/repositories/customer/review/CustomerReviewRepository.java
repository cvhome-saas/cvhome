package com.asrevo.cvhome.store.core.repositories.customer.review;

import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomerReviewRepository extends JpaRepository<CustomerReview, Long> {

    //+ "join fetch rc.attributes rca left join "
    //+ "fetch rca.customerOption rcao left join fetch rca.customerOptionValue "
    //+ "rcav left join fetch rcao.descriptions rcaod left join fetch rcav.descriptions "


    @Query("select r from CustomerReview r join fetch r.customer rc join fetch r.reviewedCustomer rr join fetch rc.merchantStore rrm left join fetch r.descriptions rd where r.id = ?1")
    CustomerReview findOne(Long id);

    @Query("select distinct r from CustomerReview r join fetch r.customer rc join fetch r.reviewedCustomer rr join fetch rc.merchantStore rrm left join fetch r.descriptions rd where rc.id = ?1")
    List<CustomerReview> findByReviewer(Long id);

    @Query("select distinct r from CustomerReview r join fetch r.customer rc join fetch r.reviewedCustomer rr join fetch rc.merchantStore rrm left join fetch r.descriptions rd where rr.id = ?1")
    List<CustomerReview> findByReviewed(Long id);

    @Query(
            """
                    select distinct r from CustomerReview r join fetch
                    r.customer rc
                    join fetch r.reviewedCustomer rr join fetch rc.merchantStore rrm
                    left join fetch r.descriptions rd
                    where rc.id = ?1 and rr.id = ?2
                    """)
    CustomerReview findByRevieweAndReviewed(Long reviewer, Long reviewed);


}
