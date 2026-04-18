package com.asrevo.cvhome.checkout.entity.customer.review;

import java.io.Serial;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.checkout.entity.customer.Customer;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "CUSTOMER_REVIEW",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"CUSTOMERS_ID", "REVIEWED_CUSTOMER_ID"})})
@Getter
@Setter
public class CustomerReview extends SalesManagerEntity<Long, CustomerReview> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_REVIEW_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CUSTOMER_REVIEW_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
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

    @ManyToOne
    @JoinColumn(name = "CUSTOMERS_ID")
    private Customer customer;

    @OneToOne
    @JoinColumn(name = "REVIEWED_CUSTOMER_ID")
    private Customer reviewedCustomer;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "customerReview")
    private Set<CustomerReviewDescription> descriptions = new HashSet<>();

}
