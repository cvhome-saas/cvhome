package com.asrevo.cvhome.catalog.entity.product.attribute;

import com.asrevo.cvhome.catalog.entity.product.type.ProductType;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
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

/**
 * Create a list of option and option value in order to accelerate and
 * prepare product attribute creation
 *
 * @author carlsamson
 */
@Entity
@Table(
        name = "PRODUCT_OPTION_SET",
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "PRODUCT_OPTION_SET_CODE"})
        })
@Getter
@Setter
public class ProductOptionSet extends SalesManagerEntity<Long, ProductOptionSet> {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_OPTION_SET_ID")
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_OPTION_SET_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_-]*$")
    @Column(name = "PRODUCT_OPTION_SET_CODE")
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_OPTION_ID", nullable = false)
    private ProductOption option;

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = ProductOptionValue.class)
    @JoinTable(name = "PRODUCT_OPT_SET_OPT_VALUE")
    private List<ProductOptionValue> values = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY, targetEntity = ProductType.class)
    @JoinTable(name = "PRODUCT_OPT_SET_PRD_TYPE")
    private Set<ProductType> productTypes = new HashSet<>();

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(
                    name = "storeMerchantId",
                    column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    @Column(name = "PRODUCT_OPTION_SET_DISP")
    private boolean optionDisplayOnly = false;
}
