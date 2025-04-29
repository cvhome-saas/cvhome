package com.asrevo.cvhome.catalog.services.product.relationship;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.catalog.repositories.product.relationship.ProductRelationshipRepository;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("productRelationshipService")
public class ProductRelationshipServiceImpl
        extends SalesManagerEntityServiceImpl<Long, ProductRelationship>
        implements ProductRelationshipService {

    private final ProductRelationshipRepository productRelationshipRepository;

    @Autowired
    public ProductRelationshipServiceImpl(
            ProductRelationshipRepository productRelationshipRepository) {
        super(productRelationshipRepository);
        this.productRelationshipRepository = productRelationshipRepository;
    }

    @Override
    public void saveOrUpdate(ProductRelationship relationship) throws ServiceException {

        if (relationship.getId() != null && relationship.getId() > 0) {

            this.update(relationship);

        } else {
            this.create(relationship);
        }
    }

    @Override
    public void addGroup(StoreMerchantId store, String groupName) {
        ProductRelationship relationship = new ProductRelationship();
        relationship.setCode(groupName);
        relationship.setStoreMerchantId(store);
        relationship.setActive(true);
        this.save(relationship);
    }

    @Override
    public List<ProductRelationship> getGroups(StoreMerchantId store) {
        return productRelationshipRepository.getGroups(store);
    }

    @Override
    public ProductRelationship getGroup(StoreMerchantId store, String code) {
        return productRelationshipRepository.getGroup(store, code);
    }

    @Override
    public void deleteGroup(StoreMerchantId store, String groupName) throws ServiceException {
        List<ProductRelationship> entities =
                productRelationshipRepository.getByGroup(store, groupName);
        for (ProductRelationship relation : entities) {
            this.delete(relation);
        }
    }

    @Override
    public void deactivateGroup(StoreMerchantId store, String groupName) throws ServiceException {
        List<ProductRelationship> entities = getGroupDefinition(store, groupName);
        for (ProductRelationship relation : entities) {
            relation.setActive(false);
            this.saveOrUpdate(relation);
        }
    }

    @Override
    public void activateGroup(StoreMerchantId store, String groupName) throws ServiceException {
        List<ProductRelationship> entities = getGroupDefinition(store, groupName);
        for (ProductRelationship relation : entities) {
            relation.setActive(true);
            this.saveOrUpdate(relation);
        }
    }

    public void deleteRelationship(ProductRelationship relationship) throws ServiceException {

        // throws detached exception so need to query first
        relationship = this.getById(relationship.getId());
        if (relationship != null) {
            delete(relationship);
        }
    }

    @Override
    public List<ProductRelationship> listByProduct(Product product) {

        return productRelationshipRepository.listByProducts(product);
    }

    @Override
    public List<ProductRelationship> getByGroup(StoreMerchantId store, String groupName) {

        return productRelationshipRepository.getByType(store, groupName);
    }

    @Override
    public List<ProductRelationship> getByGroup(
            StoreMerchantId store, String groupName, LanguageCode language) {

        return productRelationshipRepository.getByType(store, groupName, language);
    }

    @Transactional
    @Override
    public int deleteRelationship(
            Product relatedProduct, String code, StoreMerchantId storeMerchantId) {
        return productRelationshipRepository.deleteAllByRelatedProduct_IdAndCodeAndStoreMerchantId(
                relatedProduct.getId(), code, storeMerchantId);
    }

    @Transactional
    @Override
    public int deleteRelationship(
            Product relatedProduct, Product product, String code, StoreMerchantId storeMerchantId) {
        return productRelationshipRepository
                .deleteAllByRelatedProduct_IdAndProduct_IdAndCodeAndStoreMerchantId(
                        relatedProduct.getId(), product.getId(), code, storeMerchantId);
    }

    @Override
    public List<ProductRelationship> getGroupDefinition(StoreMerchantId store, String name) {
        return productRelationshipRepository.getByGroup(store, name);
    }

    @Override
    public List<ProductRelationship> getByType(
            StoreMerchantId store, Product product, String name, LanguageCode language) {
        return productRelationshipRepository.getByTypeAndRelatedProduct(
                store, name, product, language);
    }

    @Override
    public List<ProductRelationship> getByType(
            StoreMerchantId store,
            String name,
            Product product,
            Product related,
            LanguageCode language) {
        return productRelationshipRepository.getByTypeAndRelatedProduct(
                store, name, product, related, language);
    }
}
