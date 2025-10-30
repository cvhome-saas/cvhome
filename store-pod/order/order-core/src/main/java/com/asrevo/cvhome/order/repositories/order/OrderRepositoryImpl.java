package com.asrevo.cvhome.order.repositories.order;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.order.OrderList;
import com.asrevo.cvhome.order.model.order.OrderCriteria;
import com.asrevo.cvhome.store.core.entity.common.CriteriaOrderBy;
import com.asrevo.cvhome.store.core.entity.common.GenericEntityList;
import com.asrevo.cvhome.store.core.entity.order.orderstatus.OrderStatus;
import com.asrevo.cvhome.store.core.utils.RepositoryHelper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.apache.commons.lang3.StringUtils;

public class OrderRepositoryImpl implements OrderRepositoryCustom {

	@PersistenceContext
	private EntityManager em;

	@Override
	public OrderList listOrders(StoreMerchantId store, OrderCriteria criteria) {
		OrderList orderList = new OrderList();
		StringBuilder countBuilderSelect = new StringBuilder();
		StringBuilder objectBuilderSelect = new StringBuilder();

		String orderByCriteria = " order by o.id desc";

		if (criteria.getOrderBy() != null) {
			if (CriteriaOrderBy.ASC.name().equals(criteria.getOrderBy().name())) {
				orderByCriteria = " order by o.id asc";
			}
		}

		String baseQuery = """
				select o from Order as o
				left join fetch o.orderTotal ot""";
		String countBaseQuery = "select count(o) from Order as o";

		countBuilderSelect.append(countBaseQuery);
		objectBuilderSelect.append(baseQuery);

		StringBuilder objectBuilderWhere = new StringBuilder();

		String storeQuery = " where o.store=:storeId";
		objectBuilderWhere.append(storeQuery);
		countBuilderSelect.append(storeQuery);

		if (!StringUtils.isEmpty(criteria.getCustomerName())) {
			String nameQuery = " and o.billing.firstName like:name or o.billing.lastName like:name";
			objectBuilderWhere.append(nameQuery);
			countBuilderSelect.append(nameQuery);
		}

		if (!StringUtils.isEmpty(criteria.getEmail())) {
			String nameQuery = " and o.customerEmailAddress like:email";
			objectBuilderWhere.append(nameQuery);
			countBuilderSelect.append(nameQuery);
		}

		// id
		if (criteria.getId() != null) {
			String nameQuery = " and str(o.id) like:id";
			objectBuilderWhere.append(nameQuery);
			countBuilderSelect.append(nameQuery);
		}

		// phone
		if (!StringUtils.isEmpty(criteria.getCustomerPhone())) {
			String nameQuery = " and o.billing.telephone like:phone or o.delivery.telephone like:phone";
			objectBuilderWhere.append(nameQuery);
			countBuilderSelect.append(nameQuery);
		}

		// status
		if (!StringUtils.isEmpty(criteria.getStatus())) {
			String nameQuery = " and o.status =:status";
			objectBuilderWhere.append(nameQuery);
			countBuilderSelect.append(nameQuery);
		}

		objectBuilderWhere.append(orderByCriteria);

		// count query
		Query countQ = em.createQuery(countBuilderSelect.toString());

		// object query
		Query objectQ = em.createQuery(objectBuilderSelect + objectBuilderWhere.toString());

		// customer name
		if (!StringUtils.isEmpty(criteria.getCustomerName())) {
			countQ.setParameter("name", like(criteria.getCustomerName()));
			objectQ.setParameter("name", like(criteria.getCustomerName()));
		}

		// email
		if (!StringUtils.isEmpty(criteria.getEmail())) {
			countQ.setParameter("email", like(criteria.getEmail()));
			objectQ.setParameter("email", like(criteria.getEmail()));
		}

		// id
		if (criteria.getId() != null) {
			countQ.setParameter("id", like(String.valueOf(criteria.getId())));
			objectQ.setParameter("id", like(String.valueOf(criteria.getId())));
		}

		// phone
		if (!StringUtils.isEmpty(criteria.getCustomerPhone())) {
			countQ.setParameter("phone", like(criteria.getCustomerPhone()));
			objectQ.setParameter("phone", like(criteria.getCustomerPhone()));
		}

		// status
		if (!StringUtils.isEmpty(criteria.getStatus())) {
			countQ.setParameter("status", OrderStatus.valueOf(criteria.getStatus().toUpperCase()));
			objectQ.setParameter("status", OrderStatus.valueOf(criteria.getStatus().toUpperCase()));
		}

		countQ.setParameter("storeId", store);
		objectQ.setParameter("storeId", store);

		Number count = (Number) countQ.getSingleResult();

		if (count.intValue() == 0)
			return orderList;

		@SuppressWarnings("rawtypes")
		GenericEntityList entityList = new GenericEntityList();
		entityList.setTotalCount(count.intValue());

		objectQ = RepositoryHelper.paginateQuery(objectQ, count, entityList, criteria);

		// TODO use GenericEntityList

		orderList.setTotalCount(entityList.getTotalCount());
		orderList.setTotalPages(entityList.getTotalPages());

		// noinspection unchecked
		orderList.setOrders(objectQ.getResultList());

		return orderList;
	}

	private String like(String q) {
		return '%' + q + '%';
	}

}
