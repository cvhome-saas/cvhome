package com.asrevo.cvhome.store.core.entity.catalog.product.relationship;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PRODUCT_RELATIONSHIP")
@Getter
@Setter
public class ProductRelationship extends SalesManagerEntity<Long, ProductRelationship>
        implements Serializable {
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_RELATIONSHIP_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_RELATION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @ManyToOne(targetEntity = MerchantStore.class)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore store;

    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "PRODUCT_ID", updatable = false)
    private Product product = null;

    @ManyToOne(targetEntity = Product.class)
    @JoinColumn(name = "RELATED_PRODUCT_ID", updatable = false)
    private Product relatedProduct = null;

    @Column(name = "CODE")
    private String code;

    @Column(name = "ACTIVE")
    private boolean active = true;

    public ProductRelationship() {}
}
