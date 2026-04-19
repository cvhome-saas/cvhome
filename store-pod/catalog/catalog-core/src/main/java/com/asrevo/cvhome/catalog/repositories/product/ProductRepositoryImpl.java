package com.asrevo.cvhome.catalog.repositories.product;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom {

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
                q.setParameter("mid", ids);
            }

            return (Product) q.getSingleResult();

        } catch (NoResultException _) {
            return null;
        }
    }

    public Product getByFriendlyUrl(StoreMerchantId store, String seUrl, Locale locale) {

        List<String> regionList = new ArrayList<>();
        regionList.add("*");
        regionList.add(locale.getCountry());

        String hql = """
                select distinct p from Product as p
                join fetch p.availabilities pa
                join fetch p.descriptions pd
                left join fetch pa.prices pap
                left join fetch pap.descriptions papd
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
                and pa.region in (:lid)
                and pd.seUrl=:seUrl
                and p.available=true and p.dateAvailable<=:dt
                order by pattr.productOptionSortOrder""";
        Query q = this.em.createQuery(hql);

        q.setParameter("store", store);
        q.setParameter("lid", regionList);
        q.setParameter("dt", new Date());
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
                join fetch p.availabilities pa
                left join fetch pa.prices pap
                join fetch p.descriptions pd
                join fetch p.categories categs
                left join fetch pap.descriptions papd
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
                join fetch p.availabilities pa
                left join fetch pa.prices pap
                join fetch p.descriptions pd
                left join fetch p.categories categs
                left join fetch pap.descriptions papd
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

        q.setParameter("mid", store);

        @SuppressWarnings("unchecked")
        List<Product> products = q.getResultList();

        return products;
    }

    private String productQueryV2() {

        String qs = """
                select distinct p from Product as p
                join fetch p.descriptions pd
                left join fetch p.availabilities pavail
                left join fetch p.type type
                left join fetch p.images images
                left join fetch pavail.prices pavailpr
                left join fetch pavailpr.descriptions pavailprdesc
                left join fetch p.categories categs
                left join fetch categs.descriptions categsd
                left join fetch p.attributes pattr
                left join fetch pattr.productOption po
                left join fetch po.descriptions pod
                left join fetch pattr.productOptionValue pov
                left join fetch pov.descriptions povd
                left join fetch p.relationships pr
                left join fetch p.manufacturer manuf
                left join fetch manuf.descriptions manufd
                left join fetch p.variants pinst
                left join fetch pinst.variation pv
                left join fetch pv.productOption pvpo
                left join fetch pv.productOptionValue pvpov
                left join fetch pvpo.descriptions pvpod
                left join fetch pvpov.descriptions pvpovd
                left join fetch pinst.variationValue pvv
                left join fetch pvv.productOption pvvpo
                left join fetch pvv.productOptionValue pvvpov
                left join fetch pvvpo.descriptions povvpod
                left join fetch pinst.availabilities pinsta
                left join fetch pinsta.prices pinstap
                left join fetch pinstap.descriptions pinstapdesc
                left join fetch pinst.productVariantGroup pinstg
                left join fetch pinstg.images pinstgimg
                left join fetch pinstgimg.descriptions
                """;
        // end variants

        return qs;
    }

    @Override
    public Product getById(Long id, StoreMerchantId store, LanguageCode language) {

        try {

            String hql = """
                    select distinct p from Product as p
                    join fetch p.descriptions pd
                    left join fetch p.availabilities pavail
                    left join fetch p.type type
                    left join fetch pavail.prices pavailpr
                    left join fetch pavailpr.descriptions pavailprdesc
                    left join fetch p.categories categs
                    left join fetch categs.descriptions categsd
                    left join fetch p.attributes pattr
                    left join fetch pattr.productOption po
                    left join fetch po.descriptions pod
                    left join fetch pattr.productOptionValue pov
                    left join fetch pov.descriptions povd
                    left join fetch p.manufacturer manuf
                    left join fetch manuf.descriptions manufd
                    left join fetch p.variants pinst
                    left join fetch pinst.variation pv
                    left join fetch pv.productOption pvpo
                    left join fetch pv.productOptionValue pvpov
                    left join fetch pvpo.descriptions pvpod
                    left join fetch pvpov.descriptions pvpovd
                    left join fetch pinst.variationValue pvv
                    left join fetch pvv.productOption pvvpo
                    left join fetch pvv.productOptionValue pvvpov
                    left join fetch pvvpo.descriptions povvpod
                    left join fetch pinst.availabilities pinsta
                    left join fetch pinsta.prices pinstap
                    left join fetch pinstap.descriptions pinstapdesc
                    left join fetch pinst.productVariantGroup pinstg
                    left join fetch pinstg.images pinstgimg
                    left join fetch pinstgimg.descriptions
                    where p.id=:productId and p.store=:id""";
            Query q = this.em.createQuery(hql);

            q.setParameter("productId", id);
            q.setParameter("id", store);

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
                    left join fetch p.availabilities pavail
                    left join fetch pavail.prices pavailpr
                    left join fetch pavailpr.descriptions pavailprdesc
                    left join fetch p.variants pinst
                    left join fetch pinst.variation pv
                    left join fetch pinst.availabilities pinsta
                    left join fetch pinsta.prices pinstap
                    left join fetch pinstap.descriptions pinstapdesc
                    where p.id=:productId and p.store=:id and pd.languageCode=:language""";
            Query q = this.em.createQuery(hql);

            q.setParameter("productId", id);
            q.setParameter("id", store);
            q.setParameter("language", language);

            return (Product) q.getSingleResult();

        } catch (NoResultException _) {
            return null;
        }
    }

}
