package com.asrevo.cvhome.inventory.entity;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.inventory.model.price.ProductPriceType;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_PRICE")
@Getter
@Setter
public class ProductPrice extends SalesManagerEntity<Long, ProductPrice> {

    public static final String DEFAULT_PRICE_CODE = "base";

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_PRICE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_PRICE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    /**
     * Tenancy carried on the row itself. The catalog schema inherited it through the availability FK; a standalone
     * inventory service needs it directly, and the migration backfills it.
     */
    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId storeMerchantId;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "productPrice", cascade = CascadeType.ALL)
    private Set<ProductPriceDescription> descriptions = new HashSet<>();

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    @Column(name = "PRODUCT_PRICE_CODE", nullable = false)
    private String code = DEFAULT_PRICE_CODE;

    @Column(name = "PRODUCT_PRICE_AMOUNT")
    private BigDecimal productPriceAmount = new BigDecimal(0);

    @Column(name = "PRODUCT_PRICE_TYPE", length = 20)
    @Enumerated(value = EnumType.STRING)
    private ProductPriceType productPriceType = ProductPriceType.ONE_TIME;

    @Column(name = "DEFAULT_PRICE")
    private boolean defaultPrice = false;

    @Column(name = "PRODUCT_PRICE_SPECIAL_ST_DATE")
    private LocalDate productPriceSpecialStartDate;

    @Column(name = "PRODUCT_PRICE_SPECIAL_END_DATE")
    private LocalDate productPriceSpecialEndDate;

    @Column(name = "PRODUCT_PRICE_SPECIAL_AMOUNT")
    private BigDecimal productPriceSpecialAmount;

    @JsonIgnore
    @ManyToOne(targetEntity = ProductAvailability.class)
    @JoinColumn(name = "PRODUCT_AVAIL_ID", nullable = false)
    private ProductAvailability productAvailability;

    @Column(name = "PRODUCT_IDENTIFIER_ID")
    private Long productIdentifierId;

}
