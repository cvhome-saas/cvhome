package com.asrevo.cvhome.store.core.services.catalog.product.review;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReview;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.catalog.product.review.ProductReviewRepository;
import com.asrevo.cvhome.store.core.services.catalog.product.ProductService;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;

@Service("productReviewService")
public class ProductReviewServiceImpl extends
        SalesManagerEntityServiceImpl<Long, ProductReview> implements
        ProductReviewService {


    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    public ProductReviewServiceImpl(
            ProductReviewRepository productReviewRepository) {
        super(productReviewRepository);
        this.productReviewRepository = productReviewRepository;
    }

    @Override
    public List<ProductReview> getByCustomer(Customer customer) {
        return productReviewRepository.findByCustomer(customer.getId());
    }

    @Override
    public List<ProductReview> getByProduct(Product product) {
        return productReviewRepository.findByProduct(product.getId());
    }

    @Override
    public ProductReview getByProductAndCustomer(Long productId, Long customerId) {
        return productReviewRepository.findByProductAndCustomer(productId, customerId);
    }

    @Override
    public List<ProductReview> getByProduct(Product product, Language language) {
        return productReviewRepository.findByProduct(product.getId(), language.getId());
    }

    private void saveOrUpdate(ProductReview review) throws ServiceException {


        Assert.notNull(review, "ProductReview cannot be null");
        Assert.notNull(review.getProduct(), "ProductReview.product cannot be null");
        Assert.notNull(review.getCustomer(), "ProductReview.customer cannot be null");


        //refresh product
        Product product = productService.getById(review.getProduct().getId());

        //ajust product rating
        Integer count = 0;
        if (product.getProductReviewCount() != null) {
            count = product.getProductReviewCount();
        }


        BigDecimal averageRating = product.getProductReviewAvg();
        if (averageRating == null) {
            averageRating = new BigDecimal(0);
        }
        //get reviews


        BigDecimal totalRating = averageRating.multiply(new BigDecimal(count));
        totalRating = totalRating.add(new BigDecimal(review.getReviewRating()));

        count = count + 1;
        double avg = totalRating.doubleValue() / count;

        product.setProductReviewAvg(new BigDecimal(avg));
        product.setProductReviewCount(count);
        super.save(review);

        productService.update(product);

        review.setProduct(product);

    }

    public void update(ProductReview review) throws ServiceException {
        this.saveOrUpdate(review);
    }

    public void create(ProductReview review) throws ServiceException {
        this.saveOrUpdate(review);
    }

    /* (non-Javadoc)
     * @see com.asrevo.cvhome.store.core.services.catalog.product.review.ProductReviewService#getByProductNoObjects(com.salesmanager.core.model.catalog.product.Product)
     */
    @Override
    public List<ProductReview> getByProductNoCustomers(Product product) {
        return productReviewRepository.findByProductNoCustomers(product.getId());
    }


}
