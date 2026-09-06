package com.asrevo.cvhome.checkout.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.checkout.domain.CartCode;
import com.asrevo.cvhome.checkout.entity.Cart;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByStoreMerchantIdAndCode(StoreMerchantId store, CartCode code);
}
