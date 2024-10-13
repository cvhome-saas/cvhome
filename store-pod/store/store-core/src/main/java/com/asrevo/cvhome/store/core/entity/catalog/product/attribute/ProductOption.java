package com.asrevo.cvhome.store.core.entity.catalog.product.attribute;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import java.io.Serial;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "PRODUCT_OPTION",
        indexes = {@Index(name = "PRD_OPTION_CODE_IDX", columnList = "PRODUCT_OPTION_CODE")},
        uniqueConstraints = @UniqueConstraint(columnNames = {"MERCHANT_ID", "PRODUCT_OPTION_CODE"}))
@Getter
@Setter
public class ProductOption extends SalesManagerEntity<Long, ProductOption> {
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_OPTION_ID")
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_OPTION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "PRODUCT_OPTION_SORT_ORD")
    private Integer productOptionSortOrder;

    @Column(name = "PRODUCT_OPTION_TYPE", length = 10)
    private String productOptionType;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productOption")
    private Set<ProductOptionDescription> descriptions = new HashSet<>();

    @Transient private List<ProductOptionDescription> descriptionsList = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @Column(name = "PRODUCT_OPTION_READ")
    private boolean readOnly;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "PRODUCT_OPTION_CODE")
    private String code;

    public ProductOption() {}

    public List<ProductOptionDescription> getDescriptionsSettoList() {
        if (descriptionsList == null || descriptionsList.isEmpty()) {
            descriptionsList = new ArrayList<>(this.getDescriptions());
        }
        return descriptionsList;
    }
}
