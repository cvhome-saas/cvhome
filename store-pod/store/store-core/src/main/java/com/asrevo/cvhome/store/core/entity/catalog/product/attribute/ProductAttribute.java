package com.asrevo.cvhome.store.core.entity.catalog.product.attribute;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import java.io.Serial;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "PRODUCT_ATTRIBUTE",
        indexes = @Index(columnList = "PRODUCT_ID"),
        uniqueConstraints = {
            @UniqueConstraint(columnNames = {"OPTION_ID", "OPTION_VALUE_ID", "PRODUCT_ID"})
        })
@Getter
@Setter
public class ProductAttribute extends SalesManagerEntity<Long, ProductAttribute>
        implements Optionable {
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_ATTRIBUTE_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_ATTR_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Column(name = "PRODUCT_ATRIBUTE_PRICE")
    private BigDecimal productAttributePrice;

    @Column(name = "PRODUCT_ATTRIBUTE_SORT_ORD")
    private Integer productOptionSortOrder;

    @Column(name = "PRODUCT_ATTRIBUTE_FREE")
    private boolean productAttributeIsFree;

    @Column(name = "PRODUCT_ATTRIBUTE_WEIGHT")
    private BigDecimal productAttributeWeight;

    @Column(name = "PRODUCT_ATTRIBUTE_DEFAULT")
    private boolean attributeDefault = false;

    @Column(name = "PRODUCT_ATTRIBUTE_REQUIRED")
    private boolean attributeRequired = false;

    /**
     * a read only attribute is considered as a core attribute addition
     */
    @Column(name = "PRODUCT_ATTRIBUTE_FOR_DISP")
    private boolean attributeDisplayOnly = false;

    @Column(name = "PRODUCT_ATTRIBUTE_DISCOUNTED")
    private boolean attributeDiscounted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_ID", nullable = false)
    private ProductOption productOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_VALUE_ID", nullable = false)
    private ProductOptionValue productOptionValue;

    /**
     * This transient object property
     * is a utility used only to submit from a free text
     */
    @Transient private String attributePrice = "0";

    /**
     * This transient object property
     * is a utility used only to submit from a free text
     */
    @Transient private String attributeSortOrder = "0";

    /**
     * This transient object property
     * is a utility used only to submit from a free text
     */
    @Transient private String attributeAdditionalWeight = "0";

    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", nullable = false)
    private Product product;

    public ProductAttribute() {}
}
