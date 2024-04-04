package com.asrevo.cvhome.store.core.entity.customer;

import com.asrevo.cvhome.store.core.entity.catalog.product.review.ProductReview;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.CredentialsReset;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.user.Group;
import com.asrevo.cvhome.store.core.utils.CloneUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;

import java.math.BigDecimal;
import java.util.*;

@Entity
@Table(name = "CUSTOMER",
        uniqueConstraints =
        @UniqueConstraint(columnNames = {"MERCHANT_ID", "CUSTOMER_NICK"}))
@Getter
@Setter
public class Customer extends SalesManagerEntity<Long, Customer> implements Auditable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "CUSTOMER_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @Embedded
    private AuditSection auditSection = new AuditSection();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "customer")
    private Set<CustomerAttribute> attributes = new HashSet<CustomerAttribute>();

    @Column(name = "CUSTOMER_GENDER", length = 1, nullable = true)
    @Enumerated(value = EnumType.STRING)
    private CustomerGender gender;


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CUSTOMER_DOB")
    private Date dateOfBirth;

    @Email
    @NotEmpty
    @Column(name = "CUSTOMER_EMAIL_ADDRESS", length = 96, nullable = false)
    private String emailAddress;

    @Column(name = "CUSTOMER_NICK", length = 96)
    private String nick;// unique username per store

    @Column(name = "CUSTOMER_COMPANY", length = 100)
    private String company;

    @JsonIgnore
    @Column(name = "CUSTOMER_PASSWORD", length = 60)
    private String password;

    @Column(name = "CUSTOMER_ANONYMOUS")
    private boolean anonymous;

    @Column(name = "REVIEW_AVG")
    private BigDecimal customerReviewAvg;

    @Column(name = "REVIEW_COUNT")
    private Integer customerReviewCount;

    @Column(name = "PROVIDER")
    private String provider;


    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Language.class)
    @JoinColumn(name = "LANGUAGE_ID", nullable = false)
    private Language defaultLanguage;


    @OneToMany(mappedBy = "customer", targetEntity = ProductReview.class)
    private List<ProductReview> reviews = new ArrayList<ProductReview>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;


    @Embedded
    private Delivery delivery = null;

    @Valid
    @Embedded
    private Billing billing = null;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.REFRESH})
    @JoinTable(name = "CUSTOMER_GROUP", joinColumns = {
            @JoinColumn(name = "CUSTOMER_ID", nullable = false, updatable = false, insertable = false)}
            ,
            inverseJoinColumns = {@JoinColumn(name = "GROUP_ID",
                    nullable = false, updatable = false, insertable = false)}
    )
    @Cascade({
            org.hibernate.annotations.CascadeType.DETACH,
            org.hibernate.annotations.CascadeType.LOCK,
            org.hibernate.annotations.CascadeType.REFRESH,
            org.hibernate.annotations.CascadeType.REPLICATE

    })
    private List<Group> groups = new ArrayList<Group>();

    @JsonIgnore
    @Transient
    private String showCustomerStateList;

    @JsonIgnore
    @Transient
    private String showBillingStateList;

    @JsonIgnore
    @Transient
    private String showDeliveryStateList;

    @Embedded
    private CredentialsReset credentialsResetRequest = null;

    public Customer() {
    }

    public Date getDateOfBirth() {
        return CloneUtils.clone(dateOfBirth);
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = CloneUtils.clone(dateOfBirth);
    }
}
