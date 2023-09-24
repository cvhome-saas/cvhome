package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("domain_orders_ref")
@Getter
@Setter
public class DomainOrdersRefEntity {
    @Column("orders_id")
    private AggregateReference<OrdersEntity, OrdersId> orders;
}
