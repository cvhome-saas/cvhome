package com.asrevo.cvhome.catalog.entity.product.availability;

import java.time.Instant;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_RESERVATION")
@Getter
@Setter
public class ProductReservation extends SalesManagerEntity<Long, ProductReservation> {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_RESERVATION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    private Long id;

    @Column(name = "SKU", nullable = false)
    private String sku;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity;

    @Column(name = "ORDER_ID", nullable = false)
    private Long orderId;

    @Column(name = "EXPIRE_AT", nullable = false)
    private Instant expireAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ProductReservationStatusEnum status = ProductReservationStatusEnum.RESERVED;

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_AVAIL_ID")
    private ProductAvailability productAvailability;
}
