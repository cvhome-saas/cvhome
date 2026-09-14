package com.asrevo.cvhome.inventory.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.asrevo.cvhome.commons.domain.Sku;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.SkuConverter;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * One sku's share of a {@link ProductReservation}: the quantity taken and the inventory row it came from.
 */
@Entity
@Table(name = "PRODUCT_RESERVATION_LINE")
@Getter
@Setter
public class ProductReservationLine extends SalesManagerEntity<Long, ProductReservationLine> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "product_reservation_line_seq")
    @SequenceGenerator(name = "product_reservation_line_seq", sequenceName = "product_reservation_line_seq",
            allocationSize = SchemaConstant.ID_ALLOCATION_SIZE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_RESERVATION_ID", nullable = false)
    private ProductReservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_AVAIL_ID")
    private Inventory inventory;

    @Column(name = "SKU", nullable = false)
    @Convert(converter = SkuConverter.class)
    private Sku sku;

    @Column(name = "QUANTITY", nullable = false)
    private int quantity;

    public ProductReservationLine() {
    }

    public ProductReservationLine(ProductReservation reservation, Inventory inventory, int quantity) {
        this.reservation = reservation;
        this.inventory = inventory;
        this.sku = inventory.getSku();
        this.quantity = quantity;
    }
}
