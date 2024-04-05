package com.asrevo.cvhome.store.core.repositories.shoppingcart;

import com.asrevo.cvhome.store.core.entity.shoppingcart.ShoppingCartAttributeItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartAttributeRepository extends JpaRepository<ShoppingCartAttributeItem, Long> {


}
