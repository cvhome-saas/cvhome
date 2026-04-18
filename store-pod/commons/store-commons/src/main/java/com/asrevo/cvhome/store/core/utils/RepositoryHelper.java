package com.asrevo.cvhome.store.core.utils;

import jakarta.persistence.Query;

import com.asrevo.cvhome.store.core.entity.common.Criteria;
import com.asrevo.cvhome.store.core.entity.common.GenericEntityList;

/**
 * Helper for Spring Data JPA
 *
 * @author carlsamson
 */
public class RepositoryHelper {

    @SuppressWarnings("rawtypes")
    public static Query paginateQuery(Query q, Number count, GenericEntityList entityList, Criteria criteria) {

        if (entityList == null) {
            entityList = new GenericEntityList();
        }
        q.setFirstResult((int) criteria.getPageable().getOffset());
        q.setMaxResults(criteria.getPageable().getPageSize());

        return q;
    }

}
