package com.asrevo.cvhome.catalog.repositories.product;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    private static final String MID_PARAM = "mid";
    private static final String PRODUCT_ID_PARAM = "productId";
    private static final String ID_PARAM = "id";

    @PersistenceContext
    private EntityManager em;

    @Override
    public Product getById(Long productId, StoreMerchantId store) {
        return get(productId, store);
    }

    private Product get(Long productId, StoreMerchantId merchant) {

        try {

            StoreMerchantId merchantId = null;
            List<StoreMerchantId> ids = new ArrayList<>();

            StringBuilder qs = new StringBuilder();
            qs.append(productQueryV2());

            qs.append("where p.id=:pid");
            if (merchant != null) {
                merchantId = merchant;
                ids.add(merchantId);
            }

            if (merchantId != null) {
                qs.append(" and p.store in (:mid)");
            }

            String hql = qs.toString();
            Query q = this.em.createQuery(hql);

            q.setParameter("pid", productId);

            if (merchantId != null) {
                q.setParameter(MID_PARAM, ids);
            }

            return (Product) q.getSingleResult();

        } catch (NoResultException _) {
            return null;
        }
    }

    public Product getByFriendlyUrl(StoreMerchantId store, String seUrl, Locale locale) {

        String hql = """
                select distinct p from Product as p
                join fetch p.descriptions pd
                left join fetch p.categories categs
                left join fetch categs.descriptions categsd
                left join fetch p.images images
                left join fetch p.attributes pattr
                left join fetch pattr.productOption po
                left join fetch po.descriptions pod
                left join fetch pattr.productOptionValue pov
                left join fetch pov.descriptions povd
                left join fetch p.manufacturer manuf
                left join fetch manuf.descriptions manufd
                left join fetch p.type type
                where p.store=:store
                and pd.seUrl=:seUrl
                and p.available=true
                order by pattr.productOptionSortOrder""";
        Query q = this.em.createQuery(hql);

        q.setParameter("store", store);
        q.setParameter("seUrl", seUrl);

        Product p = null;

        try {
            @SuppressWarnings("unchecked")
            List<Product> products = q.getResultList();
            if (products.size() > 1) {
                log.error("Found multiple products for list of criterias with main criteria [{}]", seUrl);
            }
            p = products.getFirst();
        } catch (NoResultException _) {

        }

        return p;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Product> getProductsListByCategories(Set categoryIds) {

        String hql = """
                select distinct p from Product as p
                join fetch p.descriptions pd
                join fetch p.categories categs
                left join fetch p.images images
                left join fetch p.attributes pattr
                left join fetch pattr.productOption po
                left join fetch po.descriptions pod
                left join fetch pattr.productOptionValue pov
                left join fetch pov.descriptions povd
                left join fetch p.manufacturer manuf
                left join fetch p.type type
                where categs.id in (:cid)""";
        Query q = this.em.createQuery(hql);

        q.setParameter("cid", categoryIds);

        @SuppressWarnings("unchecked")
        List<Product> products = q.getResultList();

        return products;
    }

    @Override
    public List<Product> listByStore(StoreMerchantId store) {

        String hql = """
                select p from Product as p
                join fetch p.descriptions pd
                left join fetch p.categories categs
                left join fetch p.images images
                left join fetch p.attributes pattr
                left join fetch pattr.productOption po
                left join fetch po.descriptions pod
                left join fetch pattr.productOptionValue pov
                left join fetch pov.descriptions povd
                left join fetch p.manufacturer manuf
                left join fetch manuf.descriptions manufd
                left join fetch p.type type
                where p.store=:mid""";
        Query q = this.em.createQuery(hql);

        q.setParameter(MID_PARAM, store);

        @SuppressWarnings("unchecked")
        List<Product> products = q.getResultList();

        return products;
    }

    private String productQueryV2() {

        String qs = """
                select distinct p from Product as p
                join fetch p.descriptions pd
                left join fetch p.type type
                left join fetch p.images images
                left join fetch p.categories categs
                left join fetch categs.descriptions categsd
                left join fetch p.attributes pattr
                left join fetch pattr.productOption po
                left join fetch po.descriptions pod
                left join fetch pattr.productOptionValue pov
                left join fetch pov.descriptions povd
                left join fetch p.manufacturer manuf
                left join fetch manuf.descriptions manufd
                """;

        return qs;
    }

    @Override
    public Product getById(Long id, StoreMerchantId store, LanguageCode language) {

        try {

            String hql = """
                    select distinct p from Product as p
                    join fetch p.descriptions pd
                    left join fetch p.type type
                    left join fetch p.categories categs
                    left join fetch categs.descriptions categsd
                    left join fetch p.attributes pattr
                    left join fetch pattr.productOption po
                    left join fetch po.descriptions pod
                    left join fetch pattr.productOptionValue pov
                    left join fetch pov.descriptions povd
                    left join fetch p.manufacturer manuf
                    left join fetch manuf.descriptions manufd
                    where p.id=:productId and p.store=:id""";
            Query q = this.em.createQuery(hql);

            q.setParameter(PRODUCT_ID_PARAM, id);
            q.setParameter(ID_PARAM, store);

            return (Product) q.getSingleResult();

        } catch (NoResultException _) {
            return null;
        }
    }

    @Override
    public Product getMinimalProductById(Long id, StoreMerchantId store, LanguageCode language) {
        try {
            String hql = """
                    select distinct p from Product as p
                    join fetch p.descriptions pd
                    left join fetch p.images images
                    where p.id=:productId and p.store=:id and pd.languageCode=:language""";
            Query q = this.em.createQuery(hql);

            q.setParameter(PRODUCT_ID_PARAM, id);
            q.setParameter(ID_PARAM, store);
            q.setParameter("language", language);

            return (Product) q.getSingleResult();

        } catch (NoResultException _) {
            return null;
        }
    }

}
