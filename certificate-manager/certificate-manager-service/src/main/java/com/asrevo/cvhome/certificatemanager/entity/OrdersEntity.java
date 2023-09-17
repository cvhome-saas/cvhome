package com.asrevo.cvhome.certificatemanager.entity;

import com.asrevo.cvhome.commons.domain.BaseEntity;
import com.asrevo.cvhome.commons.domain.OrdersId;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;

@Getter
@Setter
@Table("orders")
public class OrdersEntity extends BaseEntity<OrdersId> {
    private String status;
    private Instant createdAt;
    @MappedCollection(idColumn = "orders_id")
    private CertificateEntity certificate;

    @Override
    protected OrdersId generateId() {
        return OrdersId.newId();
    }
}
