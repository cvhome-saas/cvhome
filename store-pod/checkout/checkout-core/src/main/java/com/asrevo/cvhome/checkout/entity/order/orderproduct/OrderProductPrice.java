package com.asrevo.cvhome.checkout.entity.order.orderproduct;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ORDER_PRODUCT_PRICE")
@Getter
@Setter
public class OrderProductPrice implements Serializable {

    @Serial
    private static final long serialVersionUID = 3734737890163564311L;

    @Id
    @Column(name = "ORDER_PRODUCT_PRICE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "ORDER_PRODUCT_PRICE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "ORDER_PRODUCT_ID", nullable = false)
    private OrderProduct orderProduct;

    @Column(name = "PRODUCT_PRICE_CODE", nullable = false, length = 64)
    private String productPriceCode;

    @Column(name = "PRODUCT_PRICE", nullable = false)
    private BigDecimal productPrice;

    @Column(name = "PRODUCT_PRICE_SPECIAL")
    private BigDecimal productPriceSpecial;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PRD_PRICE_SPECIAL_ST_DT", length = 0)
    private Date productPriceSpecialStartDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "PRD_PRICE_SPECIAL_END_DT", length = 0)
    private Date productPriceSpecialEndDate;

    @Column(name = "DEFAULT_PRICE", nullable = false)
    private Boolean defaultPrice;

    @Column(name = "PRODUCT_PRICE_NAME")
    private String productPriceName;

}
