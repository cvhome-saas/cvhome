package com.asrevo.cvhome.catalog.entity.product.variation;

import com.asrevo.cvhome.catalog.entity.product.attribute.Optionable;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOption;
import com.asrevo.cvhome.catalog.entity.product.attribute.ProductOptionValue;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * Product configuration pre 3.0
 * Contains possible product variations
 * <p>
 * color - red
 * size - small
 *
 * @author carlsamson
 */
@Entity
@EntityListeners(value = AuditListener.class)
@Table(
        name = "PRODUCT_VARIATION",
        uniqueConstraints =
                @UniqueConstraint(
                        columnNames = {
                            "STORE_MERCHANT_ID",
                            "PRODUCT_OPTION_ID",
                            "OPTION_VALUE_ID"
                        }))
@Getter
@Setter
public class ProductVariation extends SalesManagerEntity<Long, ProductVariation>
        implements Optionable, Auditable {

    /**
     *
     */
    @Serial private static final long serialVersionUID = 1L;

    @Embedded private AuditSection auditSection = new AuditSection();

    @Id
    @Column(name = "PRODUCT_VARIATION_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_VARIATION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(
                    name = "storeMerchantId",
                    column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "PRODUCT_OPTION_ID", nullable = false)
    private ProductOption productOption;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OPTION_VALUE_ID", nullable = false)
    private ProductOptionValue productOptionValue;

    @NotEmpty
    @Column(name = "CODE", length = 100, nullable = false)
    private String code;

    @Column(name = "SORT_ORDER")
    private Integer sortOrder;

    @Column(name = "VARIANT_DEFAULT")
    private boolean variantDefault = false;
}
