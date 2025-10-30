package com.asrevo.cvhome.catalog.services.product.relationship;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityService;
import java.util.List;

public interface ProductRelationshipService extends SalesManagerEntityService<Long, ProductRelationship> {

	void saveOrUpdate(ProductRelationship relationship) throws ServiceException;

	/**
	 * Find by product and group name
	 */
	List<ProductRelationship> getByType(StoreMerchantId store, Product product, String name, LanguageCode language);

	List<ProductRelationship> getByType(StoreMerchantId store, String name, Product product, Product related,
			LanguageCode language);

	List<ProductRelationship> listByProduct(Product product);

	/**
	 * Get a list of relationship acting as groups of products
	 */
	List<ProductRelationship> getGroups(StoreMerchantId store);

	/**
	 * Get a list of relationship acting as groups of products
	 */
	ProductRelationship getGroup(StoreMerchantId store, String code);

	/**
	 * Get group by store and group name (code)
	 */
	List<ProductRelationship> getGroupDefinition(StoreMerchantId store, String name);

	/**
	 * Creates a product group
	 */
	void addGroup(StoreMerchantId store, String groupName);

	List<ProductRelationship> getByGroup(StoreMerchantId store, String groupName);

	void deleteGroup(StoreMerchantId store, String groupName) throws ServiceException;

	void deactivateGroup(StoreMerchantId store, String groupName) throws ServiceException;

	void deleteRelationship(ProductRelationship relationship) throws ServiceException;

	void activateGroup(StoreMerchantId store, String groupName) throws ServiceException;

	List<ProductRelationship> getByGroup(StoreMerchantId store, String groupName, LanguageCode language);

	int deleteRelationship(Product relatedProduct, String code, StoreMerchantId storeMerchantId);

	int deleteRelationship(Product relatedProduct, Product product, String code, StoreMerchantId storeMerchantId);

}
