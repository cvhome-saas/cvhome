package com.asrevo.cvhome.catalog.entity.product.attribute;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_OPTION_VALUE",
        indexes = {@Index(name = "PRD_OPTION_VAL_CODE_IDX", columnList = "PRODUCT_OPTION_VAL_CODE")},
        uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "PRODUCT_OPTION_VAL_CODE"}))
@Getter
@Setter
public class ProductOptionValue extends SalesManagerEntity<Long, ProductOptionValue> {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_OPTION_VALUE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PRODUCT_OPTION_VALUE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "PRODUCT_OPT_VAL_SORT_ORD")
    private Integer productOptionValueSortOrder;

    @Column(name = "PRODUCT_OPT_VAL_IMAGE")
    private String productOptionValueImage;

    @Column(name = "PRODUCT_OPT_FOR_DISP")
    private boolean productOptionDisplayOnly = false;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    @Column(name = "PRODUCT_OPTION_VAL_CODE")
    private String code;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productOptionValue")
    private Set<ProductOptionValueDescription> descriptions = new HashSet<>();

    @Transient
    private List<ProductOptionValueDescription> descriptionsList = new ArrayList<>();

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    public ProductOptionValue() {
    }

    public List<ProductOptionValueDescription> getDescriptionsSettoList() {
        if (descriptionsList == null || descriptionsList.isEmpty()) {
            descriptionsList = new ArrayList<>(this.getDescriptions());
        }
        return descriptionsList;
    }

}
