package com.asrevo.cvhome.store.core.entity.catalog.product.review;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.product.Product;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.customer.Customer;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "PRODUCT_REVIEW", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "CUSTOMERS_ID",
                "PRODUCT_ID"
        })
}
)
@Getter
@Setter
public class ProductReview extends SalesManagerEntity<Long, ProductReview> implements Auditable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "PRODUCT_REVIEW_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "PRODUCT_REVIEW_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Column(name = "REVIEWS_RATING")
    private Double reviewRating;

    @Column(name = "REVIEWS_READ")
    private Long reviewRead;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "REVIEW_DATE")
    private Date reviewDate;

    @Column(name = "STATUS")
    private Integer status;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "CUSTOMERS_ID")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "productReview")
    private Set<ProductReviewDescription> descriptions = new HashSet<ProductReviewDescription>();

    public ProductReview() {
    }


}
