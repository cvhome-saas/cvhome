package com.asrevo.cvhome.store.core.services.catalog.product.relationship;

import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationship;
import com.asrevo.cvhome.store.core.entity.catalog.product.relationship.ProductRelationshipType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;

import java.util.List;

public interface ProductRelationshipService extends
        SalesManagerEntityService<Long, ProductRelationship> {

    void saveOrUpdate(ProductRelationship relationship) throws ServiceException;

    /**
     * Get product relationship List for a given type (RELATED, FEATURED...) and language allows
     * to return the product description in the appropriate language
     *
     * @param store
     * @param product
     * @param type
     * @param language
     * @return
     * @throws ServiceException
     */
    List<ProductRelationship> getByType(MerchantStore store, Product product,
                                        ProductRelationshipType type, Language language) throws ServiceException;

    /**
     * Find by product and group name
     *
     * @param store
     * @param product
     * @param name
     * @return
     * @throws ServiceException
     */
    List<ProductRelationship> getByType(MerchantStore store, Product product,
                                        String name) throws ServiceException;

    /**
     * Get product relationship List for a given type (RELATED, FEATURED...) and a given base product
     *
     * @param store
     * @param product
     * @param type
     * @return
     * @throws ServiceException
     */
    List<ProductRelationship> getByType(MerchantStore store, Product product,
                                        ProductRelationshipType type)
            throws ServiceException;

    /**
     * Get product relationship List for a given type (RELATED, FEATURED...)
     *
     * @param store
     * @param type
     * @return
     * @throws ServiceException
     */
    List<ProductRelationship> getByType(MerchantStore store,
                                        ProductRelationshipType type) throws ServiceException;

    List<ProductRelationship> listByProduct(Product product)
            throws ServiceException;

    List<ProductRelationship> getByType(MerchantStore store,
                                        ProductRelationshipType type, Language language)
            throws ServiceException;

    /**
     * Get a list of relationship acting as groups of products
     *
     * @param store
     * @return
     */
    List<ProductRelationship> getGroups(MerchantStore store);

    /**
     * Get a list of relationship acting as groups of products
     *
     * @param store
     * @return
     */
    ProductRelationship getGroup(MerchantStore store, String code);
    /**
     * Get group by store and group name (code)
     *
     * @param store
     * @param name
     * @return
     */
    List<ProductRelationship> getGroupDefinition(MerchantStore store, String name);

    /**
     * Creates a product group
     *
     * @param groupName
     * @throws ServiceException
     */
    void addGroup(MerchantStore store, String groupName) throws ServiceException;

    List<ProductRelationship> getByGroup(MerchantStore store, String groupName)
            throws ServiceException;

    void deleteGroup(MerchantStore store, String groupName)
            throws ServiceException;

    void deactivateGroup(MerchantStore store, String groupName)
            throws ServiceException;

    void deleteRelationship(ProductRelationship relationship) throws ServiceException;

    void activateGroup(MerchantStore store, String groupName)
            throws ServiceException;

    List<ProductRelationship> getByGroup(MerchantStore store, String groupName,
                                         Language language) throws ServiceException;

}
