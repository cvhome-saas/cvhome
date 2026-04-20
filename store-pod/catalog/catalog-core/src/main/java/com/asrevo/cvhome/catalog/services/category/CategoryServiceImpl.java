package com.asrevo.cvhome.catalog.services.category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.catalog.entity.category.CategoryDescription;
import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.repositories.category.CategoryDescriptionRepository;
import com.asrevo.cvhome.catalog.repositories.category.CategoryRepository;
import com.asrevo.cvhome.catalog.repositories.category.PageableCategoryRepository;
import com.asrevo.cvhome.catalog.services.product.ProductService;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;

@Service("categoryService")
public class CategoryServiceImpl extends SalesManagerEntityServiceImpl<Long, Category> implements CategoryService {

    private final CategoryRepository categoryRepository;

    private final ProductService productService;

    private final PageableCategoryRepository pageableCategoryRepository;

    private final CategoryDescriptionRepository categoryDescriptionRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductService productService,
                               PageableCategoryRepository pageableCategoryRepository,
                               CategoryDescriptionRepository categoryDescriptionRepository) {
        super(categoryRepository);
        this.categoryRepository = categoryRepository;
        this.productService = productService;
        this.pageableCategoryRepository = pageableCategoryRepository;
        this.categoryDescriptionRepository = categoryDescriptionRepository;
    }

    public void create(Category category) throws ServiceException {

        super.create(category);
        StringBuilder lineage = new StringBuilder();
        Category parent = category.getParent();
        if (parent != null && parent.getId() != null && parent.getId() != 0) {
            // get parent category
            Category p = this.getById(parent.getId());

            lineage.append(p.getLineage()).append(category.getId()).append("/");
            category.setDepth(p.getDepth() + 1);
        } else {
            lineage.append("/").append(category.getId()).append("/");
            category.setDepth(0);
        }
        category.setLineage(lineage.toString());
        super.update(category);
    }

    @Override
    public void saveOrUpdate(Category category) throws ServiceException {

        // save or update (persist and attach entities
        if (category.getId() != null && category.getId() > 0) {
            super.update(category);
        } else {
            this.create(category);
        }
    }

    @Override
    public List<Category> getListByLineage(StoreMerchantId store, String lineage) throws ServiceException {
        try {
            return categoryRepository.findByLineage(store, lineage);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Category getBySeUrl(StoreMerchantId store, String seUrl, LanguageCode language) {
        return categoryRepository.findByFriendlyUrl(store, seUrl, language);
    }

    @Override
    public Category getByCode(StoreMerchantId storeCode, String code) throws ServiceException {

        try {
            return categoryRepository.findByCode(storeCode, code);
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Category getById(Long id, StoreMerchantId merchantId) {

        Category category = categoryRepository.findByIdAndStore(id, merchantId);

        if (category == null) {
            return null;
        }

        List<CategoryDescription> descriptions = categoryDescriptionRepository.listByCategoryId(id);
        Set<CategoryDescription> desc = new HashSet<>(descriptions);
        category.setDescriptions(desc);

        return category;
    }

    @Override
    public void delete(Category category) throws ServiceException {

        StringBuilder lineage = new StringBuilder();
        lineage.append(category.getLineage()).append(category.getId()).append(Constants.SLASH);
        List<Category> categories = this.getListByLineage(category.getStoreMerchantId(), lineage.toString());

        Category dbCategory = getById(category.getId(), category.getStoreMerchantId());

        if (dbCategory != null && dbCategory.getId().longValue() == category.getId().longValue()) {

            categories.add(dbCategory);

            Collections.reverse(categories);

            List<Long> categoryIds = new ArrayList<>();

            for (Category c : categories) {
                categoryIds.add(c.getId());
            }

            List<Product> products = productService.getProducts(categoryIds);
            // org.hibernate.Session session =
            // em.unwrap(org.hibernate.Session.class);// need to refresh the
            // session to update
            // all product
            // categories

            for (Product product : products) {
                // session.evict(product);// refresh product, so we get all
                // product categories
                Product dbProduct = productService.getById(product.getId());
                Set<Category> productCategories = dbProduct.getCategories();
                if (productCategories.size() > 1) {
                    for (Category c : categories) {
                        productCategories.remove(c);
                        productService.update(dbProduct);
                    }

                    if (product.getCategories() == null || product.getCategories().isEmpty()) {
                        productService.delete(dbProduct);
                    }

                } else {
                    productService.delete(dbProduct);
                }
            }

            Category categ = getById(category.getId(), category.getStoreMerchantId());
            categoryRepository.delete(categ);
        }
    }

    @Override
    public void addChild(Category parent, Category child) throws ServiceException {

        if (child == null || child.getStoreMerchantId() == null) {
            throw new ServiceException("Child category and merchant store should not be null");
        }

        try {

            if (parent == null) {

                child.setParent(null);
                child.setDepth(0);
                child.setLineage(new StringBuilder().append("/").append(child.getId()).append("/").toString());

            } else {

                Category p = getById(parent.getId(), parent.getStoreMerchantId()); // parent

                String lineage = p.getLineage();
                int depth = p.getDepth();

                child.setParent(p);
                child.setDepth(depth + 1);
                child.setLineage(new StringBuilder().append(lineage)
                        .append(Constants.SLASH)
                        .append(child.getId())
                        .append(Constants.SLASH)
                        .toString());
            }

            update(child);
            StringBuilder childLineage = new StringBuilder();
            childLineage.append(child.getLineage()).append(child.getId()).append("/");
            List<Category> subCategories = getListByLineage(child.getStoreMerchantId(), childLineage.toString());

            // ajust all sub categories lineages
            if (subCategories != null && !subCategories.isEmpty()) {
                for (Category subCategory : subCategories) {
                    if (!child.getId().equals(subCategory.getId())) {
                        addChild(child, subCategory);
                    }
                }
            }
        } catch (Exception e) {
            throw new ServiceException(e);
        }
    }

    @Override
    public Page<Category> getListByDepth(StoreMerchantId store, LanguageCode language, String name, int depth,
                                         Pageable pageable) {

        return pageableCategoryRepository.listByStore(store, language, name, pageable);
    }

    @Override
    public List<Category> getByProductId(Long productId, StoreMerchantId store) {
        return categoryRepository.listByProduct(store, productId);
    }

}
