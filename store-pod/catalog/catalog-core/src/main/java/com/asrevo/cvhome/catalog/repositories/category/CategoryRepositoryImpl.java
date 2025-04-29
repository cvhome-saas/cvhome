package com.asrevo.cvhome.catalog.repositories.category;

import com.asrevo.cvhome.catalog.entity.category.Category;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;

public class CategoryRepositoryImpl implements CategoryRepositoryCustom {

    @PersistenceContext private EntityManager em;

    @Override
    public List<Category> listByProduct(StoreMerchantId store, Long product) {

        String hql =
                """
                        select category from Product product
                        inner join product.categories category
                        where product.id=:id and product.store=:mid
                        group by category""";
        Query q = this.em.createQuery(hql);

        q.setParameter("id", product);
        q.setParameter("mid", store);

        @SuppressWarnings("unchecked")
        List<Category> c = q.getResultList();

        return c;
    }
}
