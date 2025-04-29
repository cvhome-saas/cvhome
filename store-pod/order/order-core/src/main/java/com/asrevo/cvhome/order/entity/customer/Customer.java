package com.asrevo.cvhome.order.entity.customer;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.order.entity.customer.attribute.CustomerAttribute;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.CredentialsReset;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "CUSTOMER",
        uniqueConstraints = @UniqueConstraint(columnNames = {"STORE_MERCHANT_ID", "CUSTOMER_NICK"}))
@Getter
@Setter
public class Customer extends SalesManagerEntity<Long, Customer> implements Auditable {
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_ID", unique = true, nullable = false)
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "CUSTOMER_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore @Embedded private AuditSection auditSection = new AuditSection();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "customer")
    private Set<CustomerAttribute> attributes = new HashSet<>();

    @Column(name = "CUSTOMER_GENDER", length = 1)
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
    private String nick; // unique username per store

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

    //    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Language.class)
    //    @JoinColumn(name = "LANGUAGE_ID", nullable = false)
    //    private Language defaultLanguage;

    @Column(name = "LANGUAGE_CODE", length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode defaultLanguageCode;

    @Embedded
    @AttributeOverrides(
            @AttributeOverride(
                    name = "storeMerchantId",
                    column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    @Embedded private Delivery delivery = null;

    @Valid @Embedded private Billing billing = null;

    @JsonIgnore @Transient private String showCustomerStateList;

    @JsonIgnore @Transient private String showBillingStateList;

    @JsonIgnore @Transient private String showDeliveryStateList;

    @Embedded private CredentialsReset credentialsResetRequest = null;

    public Customer() {}
}
