package com.asrevo.cvhome.payment.entity.payment;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "PAYMENT_CONFIGURATION")
@Getter
@Setter
public class PaymentConfiguration {

    @Id
    @Column(name = "PAYMENT_CONFIGURATION_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "PAYMENT_CONFIGURATION_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    @AttributeOverride(name = "storeMerchantId", column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId storeMerchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_TYPE")
    private PaymentType paymentType;

    @Column(name = "API_KEY")
    private String apiKey;

    @Column(name = "SECRET_KEY")
    private String secretKey;

    @Column(name = "WEBHOOK_SECRET")
    private String webhookSecret;

    @Column(name = "ENABLED")
    private boolean enabled;
}



