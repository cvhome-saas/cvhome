package com.asrevo.cvhome.store.core.entity.catalog.product.attribute;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "PRODUCT_OPTION_VALUE",
        indexes = {@Index(name = "PRD_OPTION_VAL_CODE_IDX", columnList = "PRODUCT_OPTION_VAL_CODE")},
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"MERCHANT_ID", "PRODUCT_OPTION_VAL_CODE"}))
@Getter
@Setter
public class ProductOptionValue extends SalesManagerEntity<Long, ProductOptionValue> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_OPTION_VALUE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_OPT_VAL_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "PRODUCT_OPT_VAL_SORT_ORD")
    private Integer productOptionValueSortOrder;

    @Column(name = "PRODUCT_OPT_VAL_IMAGE")
    private String productOptionValueImage;

    @Column(name = "PRODUCT_OPT_FOR_DISP")
    private boolean productOptionDisplayOnly = false;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "PRODUCT_OPTION_VAL_CODE")
    private String code;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productOptionValue")
    private Set<ProductOptionValueDescription> descriptions = new HashSet<ProductOptionValueDescription>();

    @Transient
    private List<ProductOptionValueDescription> descriptionsList = new ArrayList<ProductOptionValueDescription>();
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    public ProductOptionValue() {
    }

    public List<ProductOptionValueDescription> getDescriptionsSettoList() {
        if (descriptionsList == null || descriptionsList.size() == 0) {
            descriptionsList = new ArrayList<ProductOptionValueDescription>(this.getDescriptions());
        }
        return descriptionsList;
    }



}
