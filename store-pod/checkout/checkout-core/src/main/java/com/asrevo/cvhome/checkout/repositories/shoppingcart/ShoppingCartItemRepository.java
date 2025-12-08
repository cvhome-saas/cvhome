package com.asrevo.cvhome.checkout.repositories.shoppingcart;

import com.asrevo.cvhome.checkout.entity.shoppingcart.ShoppingCartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ShoppingCartItemRepository extends JpaRepository<ShoppingCartItem, Long> {

	@Query("""
			select i from ShoppingCartItem i
			where i.id = ?1""")
	ShoppingCartItem findOne(Long id);

	@Modifying
	@Query("""
			delete from ShoppingCartItem i
			where i.id = ?1""")
	@Override
	void deleteById(Long id);

}
