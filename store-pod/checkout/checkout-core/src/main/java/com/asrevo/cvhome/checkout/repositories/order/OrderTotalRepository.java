package com.asrevo.cvhome.checkout.repositories.order;

import org.springframework.data.jpa.repository.JpaRepository;

import com.asrevo.cvhome.checkout.entity.order.OrderTotal;

public interface OrderTotalRepository extends JpaRepository<OrderTotal, Long> {

}
