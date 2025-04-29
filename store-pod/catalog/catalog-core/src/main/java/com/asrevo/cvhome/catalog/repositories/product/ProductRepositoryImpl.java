package com.asrevo.cvhome.catalog.repositories.product;

import com.asrevo.cvhome.catalog.entity.product.Product;
import com.asrevo.cvhome.catalog.entity.product.ProductCriteria;
import com.asrevo.cvhome.catalog.entity.product.ProductList;
import com.asrevo.cvhome.catalog.entity.product.attribute.AttributeCriteria;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.entity.common.GenericEntityList;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.utils.RepositoryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class ProductRepositoryImpl implements ProductRepositoryCustom {

    @PersistenceContext private EntityManager em;

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
                // qs.append(" and merch.id=:mid");
                qs.append(" and p.store in (:mid)");
            }

            String hql = qs.toString();
            Query q = this.em.createQuery(hql);

            q.setParameter("pid", productId);

            if (merchantId != null) {
                // q.setParameter("mid", merchant.getId());
                q.setParameter("mid", ids);
            }

            return (Product) q.getSingleResult();

        } catch (jakarta.persistence.NoResultException ers) {
            return null;
        }
    }

    public Product getByFriendlyUrl(StoreMerchantId store, String seUrl, Locale locale) {

        List<String> regionList = new ArrayList<>();
        regionList.add("*");
        regionList.add(locale.getCountry());

        // images
        // options
        // other lefts
        // RENTAL
        // qs.append("left join fetch p.owner owner ");
        String hql =
                """
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
                        left join fetch p.relationships pr
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
                log.error(
                        "Found multiple products for list of criterias with main criteria [{}]",
                        seUrl);
            }
            // p = (Product)q.getSingleResult();
            p = products.getFirst();
        } catch (jakarta.persistence.NoResultException ignore) {

        }

        return p;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public List<Product> getProductsListByCategories(Set categoryIds) {

        // List regionList = new ArrayList();
        // regionList.add("*");
        // regionList.add(locale.getCountry());

        // TODO Test performance

        // images
        // options (do not need attributes for listings)
        // other lefts
        // RENTAL
        // qs.append("left join fetch p.owner owner ");
        // qs.append("where pa.region in (:lid) ");
        String hql =
                """
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

    /**
     * This query is used for filtering products based on criterias
     * Main query for getting product list based on input criteria
     * ze method
     */
    @Override
    public ProductList listByStore(
            StoreMerchantId store, LanguageCode language, ProductCriteria criteria) {

        ProductList productList = new ProductList();

        StringBuilder countBuilderSelect = new StringBuilder();
        countBuilderSelect.append("select count(distinct p) from Product as p");

        StringBuilder countBuilderWhere = new StringBuilder();
        countBuilderWhere.append(" where p.store=:mId");

        if (!CollectionUtils.isEmpty(criteria.getProductIds())) {
            countBuilderWhere.append(" and p.id in (:pId)");
        }

        countBuilderSelect.append(" inner join p.descriptions pd");
        if (criteria.getLanguage() != null && !criteria.getLanguage().code().equals("_all")) {
            countBuilderWhere.append(" and pd.languageCode=:lang");
        }

        if (!StringUtils.isBlank(criteria.getProductName())) {
            countBuilderWhere.append(" and lower(pd.name) like:nm");
        }

        if (!CollectionUtils.isEmpty(criteria.getCategoryIds())) {
            countBuilderSelect.append(" INNER JOIN p.categories categs");
            countBuilderWhere.append(" and categs.id in (:cid)");
        }

        if (criteria.getManufacturerId() != null) {
            countBuilderSelect.append(" INNER JOIN p.manufacturer manuf");
            countBuilderWhere.append(" and manuf.id = :manufid");
        }

        // todo type

        // sku
        if (!StringUtils.isBlank(criteria.getCode())) {
            countBuilderWhere.append(" and lower(p.sku) like :sku");
        }

        // RENTAL

        // attribute or option values
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                        && CollectionUtils.isNotEmpty(criteria.getAttributeCriteria())
                || CollectionUtils.isNotEmpty(criteria.getOptionValueIds())) {

            countBuilderSelect.append(" INNER JOIN p.attributes pattr");
            countBuilderSelect.append(" INNER JOIN pattr.productOption po");
            countBuilderSelect.append(" INNER JOIN pattr.productOptionValue pov ");
            countBuilderSelect.append(" INNER JOIN pov.descriptions povd ");

            if (CollectionUtils.isNotEmpty(criteria.getAttributeCriteria())) {
                int count = 0;
                for (AttributeCriteria attributeCriteria : criteria.getAttributeCriteria()) {
                    if (count == 0) {
                        countBuilderWhere
                                .append(" and po.code =:")
                                .append(attributeCriteria.getAttributeCode());
                        countBuilderWhere
                                .append(" and povd.description like :")
                                .append("val")
                                .append(count)
                                .append(attributeCriteria.getAttributeCode());
                    }
                    count++;
                }
                if (criteria.getLanguage() != null
                        && !criteria.getLanguage().code().equals("_all")) {
                    countBuilderWhere.append(" and povd.languageCode=:lang");
                }
            }

            if (CollectionUtils.isNotEmpty(criteria.getOptionValueIds())) {
                countBuilderWhere.append(" and pov.id in (:povid)");
            }
        }

        if (criteria.getAvailable() != null) {
            if (criteria.getAvailable()) {
                countBuilderWhere.append(" and p.available=true and p.dateAvailable<=:dt");
            } else {
                countBuilderWhere.append(" and p.available=false or p.dateAvailable>:dt");
            }
        }

        Query countQ = this.em.createQuery(countBuilderSelect + countBuilderWhere.toString());

        countQ.setParameter("mId", store);

        /**/
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && !CollectionUtils.isEmpty(criteria.getCategoryIds())) {
            countQ.setParameter("cid", criteria.getCategoryIds());
        }

        /**/
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && CollectionUtils.isNotEmpty(criteria.getOptionValueIds())) {
            countQ.setParameter("povid", criteria.getOptionValueIds());
        }

        if (criteria.getAvailable() != null) {
            countQ.setParameter("dt", new Date());
        }

        if (!StringUtils.isBlank(criteria.getCode())) {
            countQ.setParameter(
                    "sku",
                    new StringBuilder()
                            .append("%")
                            .append(criteria.getCode().toLowerCase())
                            .append("%")
                            .toString());
        }

        if (criteria.getManufacturerId() != null) {
            countQ.setParameter("manufid", criteria.getManufacturerId());
        }

        /**/
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && !CollectionUtils.isEmpty(criteria.getAttributeCriteria())) {
            int count = 0;
            for (AttributeCriteria attributeCriteria : criteria.getAttributeCriteria()) {
                countQ.setParameter(
                        attributeCriteria.getAttributeCode(), attributeCriteria.getAttributeCode());
                countQ.setParameter(
                        "val" + count + attributeCriteria.getAttributeCode(),
                        "%" + attributeCriteria.getAttributeValue() + "%");
                count++;
            }
        }

        if (criteria.getLanguage() != null && !criteria.getLanguage().code().equals("_all")) {
            countQ.setParameter("lang", language);
        }

        if (!StringUtils.isBlank(criteria.getProductName())) {
            countQ.setParameter(
                    "nm",
                    new StringBuilder()
                            .append("%")
                            .append(criteria.getProductName().toLowerCase())
                            .append("%")
                            .toString());
        }

        if (!CollectionUtils.isEmpty(criteria.getProductIds())) {
            countQ.setParameter("pId", criteria.getProductIds());
        }

        // RENTAL

        if (criteria.getOwnerId() != null) {
            countQ.setParameter("ownerid", criteria.getOwnerId());
        }

        Number count = (Number) countQ.getSingleResult();
        productList.setTotalCount(count.intValue());

        if (count.intValue() == 0) return productList;

        StringBuilder qs = new StringBuilder();
        qs.append("select distinct p from Product as p ");
        qs.append("join fetch p.availabilities pa ");
        qs.append("left join fetch pa.prices pap ");
        qs.append("left join fetch pap.descriptions papd ");

        qs.append("left join fetch p.descriptions pd ");
        qs.append("left join fetch p.categories categs ");
        qs.append("left join fetch categs.descriptions cd ");

        // images
        qs.append("left join fetch p.images images ");

        // other lefts
        qs.append("left join fetch p.manufacturer manuf ");
        qs.append("left join fetch manuf.descriptions manufd ");
        qs.append("left join fetch p.type type ");

        // RENTAL
        // qs.append("left join fetch p.owner owner ");

        /**/
        // attributes
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && !CollectionUtils.isEmpty(criteria.getAttributeCriteria())) {
            qs.append(" inner join p.attributes pattr");
            qs.append(" inner join pattr.productOption po");
            qs.append(" inner join po.descriptions pod");
            qs.append(" inner join pattr.productOptionValue pov ");
            qs.append(" inner join pov.descriptions povd");
        } else if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)) {
            qs.append(" left join fetch p.attributes pattr");
            qs.append(" left join fetch pattr.productOption po");
            qs.append(" left join fetch po.descriptions pod");
            qs.append(" left join fetch pattr.productOptionValue pov");
            qs.append(" left join fetch pov.descriptions povd");
        }

        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)) {
            qs.append(" left join fetch p.variants pinst ");
            qs.append(" left join fetch pinst.variation pv ");
            qs.append("left join fetch pv.productOption pvpo ");
            qs.append(" left join fetch pv.productOptionValue pvpov ");
            qs.append(" left join fetch pvpo.descriptions pvpod ");
            qs.append(" left join fetch pvpov.descriptions pvpovd ");

            qs.append(" left join fetch pinst.variationValue pvv ");
            qs.append(" left join fetch pvv.productOption pvvpo ");
            qs.append(" left join fetch pvv.productOptionValue pvvpov ");
            qs.append(" left join fetch pvvpo.descriptions povvpod ");

            // variant availability and price
            qs.append(" left join fetch pinst.availabilities pinsta ");
            qs.append(" left join fetch pinsta.prices pinstap ");
            qs.append(" left join fetch pinstap.descriptions pinstapdesc ");
            qs.append(" left join fetch pinst.productVariantGroup pinstg ");
            qs.append(" left join fetch pinstg.images pinstgimg ");
            qs.append(" left join fetch pinstgimg.descriptions ");
            // end variants
        }

        // qs.append(" left join fetch p.relationships pr");

        qs.append(" where p.store=:mId");
        if (criteria.getLanguage() != null && !criteria.getLanguage().code().equals("_all")) {
            qs.append(" and pd.languageCode=:lang");
        }

        if (!CollectionUtils.isEmpty(criteria.getProductIds())) {
            qs.append(" and p.id in (:pId)");
        }

        if (!CollectionUtils.isEmpty(criteria.getCategoryIds())) {
            qs.append(" and categs.id in (:cid)");
        }

        if (criteria.getManufacturerId() != null) {
            qs.append(" and manuf.id = :manufid");
        }

        if (criteria.getAvailable() != null) {
            if (criteria.getAvailable()) {
                qs.append(" and p.available=true and p.dateAvailable<=:dt");
            } else {
                qs.append(" and p.available=false and p.dateAvailable>:dt");
            }
        }

        if (!StringUtils.isBlank(criteria.getProductName())) {
            qs.append(" and lower(pd.name) like :nm");
        }

        if (!StringUtils.isBlank(criteria.getCode())) {
            qs.append(" and lower(p.sku) like :sku");
        }

        // RENTAL

        /**/
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && !CollectionUtils.isEmpty(criteria.getAttributeCriteria())) {
            int cnt = 0;
            for (AttributeCriteria attributeCriteria : criteria.getAttributeCriteria()) {
                qs.append(" and po.code =:").append(attributeCriteria.getAttributeCode());
                qs.append(" and povd.description like :")
                        .append("val")
                        .append(cnt)
                        .append(attributeCriteria.getAttributeCode());
                cnt++;
            }
            if (criteria.getLanguage() != null && !criteria.getLanguage().code().equals("_all")) {
                qs.append(" and povd.languageCode=:lang");
            }
        }

        /**/
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && CollectionUtils.isNotEmpty(criteria.getOptionValueIds())) {
            qs.append(" and pov.id in (:povid)");
        }

        qs.append(" order by p.sortOrder asc");

        String hql = qs.toString();
        Query q = this.em.createQuery(hql);

        if (criteria.getLanguage() != null && !criteria.getLanguage().code().equals("_all")) {
            q.setParameter("lang", language);
        }
        q.setParameter("mId", store);

        if (!CollectionUtils.isEmpty(criteria.getCategoryIds())) {
            q.setParameter("cid", criteria.getCategoryIds());
        }

        /**/
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && CollectionUtils.isNotEmpty(criteria.getOptionValueIds())) {
            q.setParameter("povid", criteria.getOptionValueIds());
        }

        if (!CollectionUtils.isEmpty(criteria.getProductIds())) {
            q.setParameter("pId", criteria.getProductIds());
        }

        if (criteria.getAvailable() != null) {
            q.setParameter("dt", new Date());
        }

        if (criteria.getManufacturerId() != null) {
            q.setParameter("manufid", criteria.getManufacturerId());
        }

        if (!StringUtils.isBlank(criteria.getCode())) {
            q.setParameter(
                    "sku",
                    new StringBuilder()
                            .append("%")
                            .append(criteria.getCode().toLowerCase())
                            .append("%")
                            .toString());
        }

        /**/
        if (criteria.getOrigin().equals(ProductCriteria.ORIGIN_SHOP)
                && !CollectionUtils.isEmpty(criteria.getAttributeCriteria())) {
            int cnt = 0;
            for (AttributeCriteria attributeCriteria : criteria.getAttributeCriteria()) {
                q.setParameter(
                        attributeCriteria.getAttributeCode(), attributeCriteria.getAttributeCode());
                q.setParameter(
                        "val" + cnt + attributeCriteria.getAttributeCode(),
                        "%" + attributeCriteria.getAttributeValue() + "%");
                cnt++;
            }
        }

        // RENTAL

        if (!StringUtils.isBlank(criteria.getProductName())) {
            q.setParameter(
                    "nm",
                    new StringBuilder()
                            .append("%")
                            .append(criteria.getProductName().toLowerCase())
                            .append("%")
                            .toString());
        }

        @SuppressWarnings("rawtypes")
        GenericEntityList entityList = new GenericEntityList();
        entityList.setTotalCount(count.intValue());

        q = RepositoryHelper.paginateQuery(q, count, entityList, criteria);

        @SuppressWarnings("unchecked")
        List<Product> products = q.getResultList();
        productList.setProducts(products);

        return productList;
    }

    @Override
    public List<Product> listByStore(StoreMerchantId store) {

        // images
        // options (do not need attributes for listings)
        // other lefts
        // RENTAL
        // qs.append("left join fetch p.owner owner ");
        // qs.append("where pa.region in (:lid) ");
        String hql =
                """
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

        // images
        // options
        // other lefts
        // variants
        // variant availability and price
        String qs =
                """
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

            // options
            // other lefts
            // variants
            // variant availability and price
            // end variants
            String hql =
                    """
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
                            where p.id=:productId and p.store=:id""";
            Query q = this.em.createQuery(hql);

            q.setParameter("productId", id);
            q.setParameter("id", store);

            return (Product) q.getSingleResult();

        } catch (jakarta.persistence.NoResultException ers) {
            return null;
        }
    }
}
