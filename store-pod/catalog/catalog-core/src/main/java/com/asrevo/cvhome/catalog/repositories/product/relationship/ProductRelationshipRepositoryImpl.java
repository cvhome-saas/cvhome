package com.asrevo.cvhome.catalog.repositories.product.relationship;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.relationship.ProductRelationship;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProductRelationshipRepositoryImpl implements ProductRelationshipRepositoryCustom {

    private static final String HQL_GET_PRODUCTS_BY_PRODUCT_ID =
            """
                    select distinct pr from ProductRelationship as pr
                    left join fetch pr.product p
                    left join fetch pr.relatedProduct rp
                    left join fetch rp.attributes pattr
                    left join fetch rp.categories rpc
                    left join fetch p.descriptions pd
                    left join fetch rp.descriptions rpd
                    where p.id=:id
                     or rp.id=:id""";
    private static final String HQL_GET_PRODUCT_BY_CODE_AND_STORE_ID =
            """
                    select distinct pr from ProductRelationship as pr
                    left join fetch pr.product p
                    join fetch pr.relatedProduct rp
                    left join fetch rp.descriptions rpd
                    where pr.code=:code
                    and pr.storeMerchantId=:storeId""";
    private static final String HQL_GET_GROUP_BY_CODE_AND_STORE_ID =
            """
                    select distinct pr from ProductRelationship as pr
                    left join fetch pr.product p
                    left join fetch pr.relatedProduct rp
                    left join fetch rp.attributes pattr
                    left join fetch rp.categories rpc
                    left join fetch rpc.descriptions rpcd
                    left join fetch rp.descriptions rpd
                    left join fetch rp.images pd
                    left join fetch rp.availabilities pa
                    left join fetch pa.prices pap
                    left join fetch pap.descriptions papd
                    left join fetch rp.manufacturer manuf
                    left join fetch manuf.descriptions manufd
                    left join fetch rp.type type
                    where pr.code=:code
                    and pr.storeMerchantId=:storeId""";
    private static final String HQL_GET_PRODUCT_RELATIONSHIP_BY_STORE_ID =
            """
                                select distinct pr from ProductRelationship as pr
                                where pr.storeMerchantId=:store
                                and pr.product is null
                    """;
    private static final String HQL_GET_PRODUCT_RELATIONSHIP_BY_STORE_ID_AND_CODE =
            """
                                select distinct pr from ProductRelationship as pr
                                where pr.storeMerchantId=:store
                                and pr.code=:code
                                and pr.product is null
                    """;

    @PersistenceContext private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductRelationship> getByGroup(StoreMerchantId store, String group) {
        return entityManager
                .createQuery(HQL_GET_GROUP_BY_CODE_AND_STORE_ID)
                .setParameter("code", group)
                .setParameter("storeId", store)
                .getResultList();
    }

    @Override
    public List<ProductRelationship> getGroups(StoreMerchantId store) {
        @SuppressWarnings("unchecked")
        List<ProductRelationship> relations =
                entityManager
                        .createQuery(HQL_GET_PRODUCT_RELATIONSHIP_BY_STORE_ID)
                        .setParameter("store", store)
                        .getResultList();
        Map<String, ProductRelationship> relationMap =
                relations.stream()
                        .collect(
                                Collectors.toMap(
                                        ProductRelationship::getCode, p -> p, (p, q) -> p));
        return relationMap.values().stream().toList();
    }

    @Override
    public ProductRelationship getGroup(StoreMerchantId store, String code) {
        return (ProductRelationship)
                entityManager
                        .createQuery(HQL_GET_PRODUCT_RELATIONSHIP_BY_STORE_ID_AND_CODE)
                        .setParameter("store", store)
                        .setParameter("code", code)
                        .setMaxResults(1)
                        .getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductRelationship> getByType(StoreMerchantId store, String type) {
        return entityManager
                .createQuery(HQL_GET_PRODUCT_BY_CODE_AND_STORE_ID)
                .setParameter("code", type)
                .setParameter("storeId", store)
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductRelationship> listByProducts(Product product) {
        return entityManager
                .createQuery(HQL_GET_PRODUCTS_BY_PRODUCT_ID)
                .setParameter("id", product.getId())
                .getResultList();
    }
}
