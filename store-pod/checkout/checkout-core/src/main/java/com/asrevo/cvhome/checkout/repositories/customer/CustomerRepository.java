package com.asrevo.cvhome.checkout.repositories.customer;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

	@Query("""
			select c from Customer c
			where c.id = ?1""")
	Customer findOne(Long id);

	Optional<Customer> findByCuaExternalId(String cuaExternalId);

}
