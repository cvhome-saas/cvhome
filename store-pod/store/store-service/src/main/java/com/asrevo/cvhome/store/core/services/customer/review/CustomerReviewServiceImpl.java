package com.asrevo.cvhome.store.core.services.customer.review;

import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.customer.review.CustomerReview;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.customer.review.CustomerReviewRepository;
import com.asrevo.cvhome.store.core.services.customer.CustomerService;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

@Service("customerReviewService")
public class CustomerReviewServiceImpl extends SalesManagerEntityServiceImpl<Long, CustomerReview>
        implements CustomerReviewService {

    private final CustomerReviewRepository customerReviewRepository;

    private final CustomerService customerService;

    @Autowired
    public CustomerReviewServiceImpl(
            CustomerReviewRepository customerReviewRepository, CustomerService customerService) {
        super(customerReviewRepository);
        this.customerReviewRepository = customerReviewRepository;
        this.customerService = customerService;
    }

    private void saveOrUpdate(CustomerReview review) throws ServiceException {

        Assert.notNull(review, "CustomerReview cannot be null");
        Assert.notNull(review.getCustomer(), "CustomerReview.customer cannot be null");
        Assert.notNull(
                review.getReviewedCustomer(), "CustomerReview.reviewedCustomer cannot be null");

        // refresh customer
        Customer customer = customerService.getById(review.getReviewedCustomer().getId());

        // ajust product rating
        int count = 0;
        if (customer.getCustomerReviewCount() != null) {
            count = customer.getCustomerReviewCount();
        }

        BigDecimal averageRating = customer.getCustomerReviewAvg();
        if (averageRating == null) {
            averageRating = new BigDecimal(0);
        }
        // get reviews

        BigDecimal totalRating = averageRating.multiply(new BigDecimal(count));
        totalRating = totalRating.add(BigDecimal.valueOf(review.getReviewRating()));

        count = count + 1;
        double avg = totalRating.doubleValue() / count;

        customer.setCustomerReviewAvg(new BigDecimal(avg));
        customer.setCustomerReviewCount(count);
        super.save(review);

        customerService.update(customer);

        review.setReviewedCustomer(customer);
    }

    public void update(CustomerReview review) throws ServiceException {
        this.saveOrUpdate(review);
    }

    public void create(CustomerReview review) throws ServiceException {
        this.saveOrUpdate(review);
    }

    @Override
    public List<CustomerReview> getByCustomer(Customer customer) {
        Assert.notNull(customer, "Customer cannot be null");
        return customerReviewRepository.findByReviewer(customer.getId());
    }

    @Override
    public List<CustomerReview> getByReviewedCustomer(Customer customer) {
        Assert.notNull(customer, "Customer cannot be null");
        return customerReviewRepository.findByReviewed(customer.getId());
    }

    @Override
    public CustomerReview getByReviewerAndReviewed(Long reviewer, Long reviewed) {
        Assert.notNull(reviewer, "Reviewer customer cannot be null");
        Assert.notNull(reviewed, "Reviewer customer cannot be null");
        return customerReviewRepository.findByRevieweAndReviewed(reviewer, reviewed);
    }
}
