package com.asrevo.cvhome.store.core.entity.order.orderproduct;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "ORDER_PRODUCT_PRICE")
@Getter
@Setter
public class OrderProductPrice implements Serializable {
    private static final long serialVersionUID = 3734737890163564311L;

    @Id
    @Column(name = "ORDER_PRODUCT_PRICE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "ORDER_PRD_PRICE_ID_NEXT_VAL")
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


    @Column(name = "PRODUCT_PRICE_NAME", nullable = true)
    private String productPriceName;

}
