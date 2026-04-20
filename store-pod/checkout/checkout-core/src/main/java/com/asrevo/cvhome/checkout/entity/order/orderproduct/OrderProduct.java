package com.asrevo.cvhome.checkout.entity.order.orderproduct;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.checkout.entity.order.Order;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_PRODUCT")
@Getter
@Setter
public class OrderProduct extends SalesManagerEntity<Long, OrderProduct> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ORDER_PRODUCT_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_PRODUCT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "PRODUCT_SKU")
    private String sku;

    @Column(name = "PRODUCT_NAME", length = 64, nullable = false)
    private String productName;

    @Column(name = "PRODUCT_QUANTITY")
    private int productQuantity;

    @Column(name = "ONETIME_CHARGE", nullable = false)
    private BigDecimal oneTimeCharge;

    @JsonIgnore
    @ManyToOne(targetEntity = Order.class)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @OneToMany(mappedBy = "orderProduct", cascade = CascadeType.ALL)
    private Set<OrderProductAttribute> orderAttributes = new HashSet<>();

    @OneToMany(mappedBy = "orderProduct", cascade = CascadeType.ALL)
    private Set<OrderProductPrice> prices = new HashSet<>();

    @OneToMany(mappedBy = "orderProduct", cascade = CascadeType.ALL)
    private Set<OrderProductDownload> downloads = new HashSet<>();
}
