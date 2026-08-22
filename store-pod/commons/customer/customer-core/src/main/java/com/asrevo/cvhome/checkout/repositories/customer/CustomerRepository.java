package com.asrevo.cvhome.checkout.repositories.customer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.checkout.entity.customer.CustomerCriteria;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

import static com.asrevo.cvhome.store.core.constants.Constants.PERCENT_SYMBOL;

public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> {

    @Query("""
            select c from Customer c
            where c.id = ?1""")
    Customer findOne(Long id);

    Optional<Customer> findByCuaExternalId(String cuaExternalId);

    default Page<Customer> findByStoreMerchantId(StoreMerchantId store, CustomerCriteria criteria, Pageable pageable) {

        return findAll((root, cq, cb) -> {

            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("storeMerchantId"), store));

            Path<Object> billing = root.get("billing");
            Path<String> firstName = billing.get("firstName");
            Path<String> lastName = billing.get("lastName");
            // The one query a single search box sends. It spans the email as well as the two name parts
            // because the predicates below are AND-ed: a box sending `name` and `email` together would
            // match nothing. The result used to be computed and dropped rather than added, so a name
            // filter narrowed nothing at all.
            if (!StringUtils.isBlank(criteria.getName())) {
                String likeValue = PERCENT_SYMBOL + criteria.getName() + PERCENT_SYMBOL;
                predicates.add(cb.or(cb.like(firstName, likeValue), cb.like(lastName, likeValue),
                        cb.like(root.get("emailAddress"), likeValue)));
            }
            if (!StringUtils.isBlank(criteria.getFirstName())) {
                predicates.add(cb.like(firstName, PERCENT_SYMBOL + criteria.getFirstName() + PERCENT_SYMBOL));
            }
            if (!StringUtils.isBlank(criteria.getLastName())) {
                predicates.add(cb.like(lastName, PERCENT_SYMBOL + criteria.getLastName() + PERCENT_SYMBOL));
            }
            if (!StringUtils.isBlank(criteria.getEmail())) {
                predicates.add(cb.like(root.get("emailAddress"), PERCENT_SYMBOL + criteria.getEmail() + PERCENT_SYMBOL));
            }
            if (!StringUtils.isBlank(criteria.getCountry())) {
                predicates
                        .add(cb.like(billing.get("country").get("isoCode"), PERCENT_SYMBOL + criteria.getCountry() + PERCENT_SYMBOL));
            }

            return cb.and(predicates);
        }, pageable);
    }

}
