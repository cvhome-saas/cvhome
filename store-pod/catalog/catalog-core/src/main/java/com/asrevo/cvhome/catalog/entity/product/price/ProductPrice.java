package com.asrevo.cvhome.catalog.entity.product.price;

import com.asrevo.cvhome.catalog.entity.product.availability.ProductAvailability;
import com.asrevo.cvhome.catalog.model.product.product.price.ProductPriceType;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.utils.CloneUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_PRICE")
@Getter
@Setter
public class ProductPrice extends SalesManagerEntity<Long, ProductPrice> {
    public static final String DEFAULT_PRICE_CODE = "base";
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_PRICE_ID")
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_PRICE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productPrice", cascade = CascadeType.ALL)
    private Set<ProductPriceDescription> descriptions = new HashSet<>();

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "PRODUCT_PRICE_CODE", nullable = false)
    private String code = DEFAULT_PRICE_CODE;

    @Column(name = "PRODUCT_PRICE_AMOUNT")
    private BigDecimal productPriceAmount = new BigDecimal(0);

    @Column(name = "PRODUCT_PRICE_TYPE", length = 20)
    @Enumerated(value = EnumType.STRING)
    private ProductPriceType productPriceType = ProductPriceType.ONE_TIME;

    @Column(name = "DEFAULT_PRICE")
    private boolean defaultPrice = false;

    @Temporal(TemporalType.DATE)
    @Column(name = "PRODUCT_PRICE_SPECIAL_ST_DATE")
    private Date productPriceSpecialStartDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "PRODUCT_PRICE_SPECIAL_END_DATE")
    private Date productPriceSpecialEndDate;

    @Column(name = "PRODUCT_PRICE_SPECIAL_AMOUNT")
    private BigDecimal productPriceSpecialAmount;

    @JsonIgnore
    @ManyToOne(targetEntity = ProductAvailability.class)
    @JoinColumn(name = "PRODUCT_AVAIL_ID", nullable = false)
    private ProductAvailability productAvailability;

    @Column(name = "PRODUCT_IDENTIFIER_ID")
    private Long productIdentifierId;

    public ProductPrice() {}

    public Date getProductPriceSpecialStartDate() {
        return CloneUtils.clone(productPriceSpecialStartDate);
    }

    public void setProductPriceSpecialStartDate(Date productPriceSpecialStartDate) {
        this.productPriceSpecialStartDate = CloneUtils.clone(productPriceSpecialStartDate);
    }

    public Date getProductPriceSpecialEndDate() {
        return CloneUtils.clone(productPriceSpecialEndDate);
    }

    public void setProductPriceSpecialEndDate(Date productPriceSpecialEndDate) {
        this.productPriceSpecialEndDate = CloneUtils.clone(productPriceSpecialEndDate);
    }
}
