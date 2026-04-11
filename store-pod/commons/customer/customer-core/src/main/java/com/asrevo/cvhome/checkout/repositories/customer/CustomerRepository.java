package com.asrevo.cvhome.checkout.repositories.customer;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

	@Query("""
			select c from Customer c
			where c.id = ?1""")
	Customer findOne(Long id);

	Optional<Customer> findByCuaExternalId(String cuaExternalId);

	default Page<Customer> findByStoreMerchantId(StoreMerchantId store, CustomerCriteria criteria) {

		return findAll((root, cq, cb) -> {

			List<Predicate> predicates = new ArrayList<>();
			predicates.add(cb.equal(root.get("storeMerchantId"), store));

			if (!StringUtils.isBlank(criteria.getName())) {
				cb.or(cb.like(root.get("billing").get("firstName"), "%" + criteria.getName() + "%"),
						cb.like(root.get("billing").get("lastName"), "%" + criteria.getName() + "%"));
			}
			if (!StringUtils.isBlank(criteria.getFirstName())) {
				predicates.add(cb.like(root.get("billing").get("firstName"), "%" + criteria.getFirstName() + "%"));
			}
			if (!StringUtils.isBlank(criteria.getLastName())) {
				predicates.add(cb.like(root.get("billing").get("lastName"), "%" + criteria.getLastName() + "%"));
			}
			if (!StringUtils.isBlank(criteria.getEmail())) {
				predicates.add(cb.like(root.get("emailAddress"), "%" + criteria.getEmail() + "%"));
			}
			if (!StringUtils.isBlank(criteria.getCountry())) {
				predicates
					.add(cb.like(root.get("billing").get("country").get("isoCode"), "%" + criteria.getCountry() + "%"));
			}

			return cb.and(predicates);
		}, criteria.getPageable());
	}

}
