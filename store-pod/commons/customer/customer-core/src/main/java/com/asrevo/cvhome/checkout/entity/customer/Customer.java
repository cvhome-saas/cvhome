package com.asrevo.cvhome.checkout.entity.customer;

import java.io.Serial;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.Billing;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CUSTOMER")
@Getter
@Setter
public class Customer extends SalesManagerEntity<Long, Customer> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CUSTOMER_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Email
    @NotEmpty
    @Column(name = "CUSTOMER_EMAIL_ADDRESS", length = 96)
    private String emailAddress;

    @Column(name = "CUSTOMER_USERNAME", length = 96)
    private String username;

    @Column(name = "CUA_EXTERNAL_ID", length = 96)
    private String cuaExternalId;

    @Column(name = "LANGUAGE_CODE", length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode defaultLanguageCode;

    @Embedded
    @AttributeOverrides(@AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50)))
    private StoreMerchantId storeMerchantId;

    @Embedded
    private Delivery delivery = null;

    @Valid
    @Embedded
    private Billing billing = null;

    public Customer() {
    }

}
