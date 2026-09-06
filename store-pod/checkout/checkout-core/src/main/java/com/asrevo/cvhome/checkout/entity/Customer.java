package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditListener;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;

import lombok.Getter;
import lombok.Setter;

/**
 * A shopper as this store knows them: the cua account id they sign in with, and the addresses they last ordered
 * with. Unique per (store, cua id) — the same cua account in two stores is two customers, because two stores share
 * nothing.
 */
@Entity
@EntityListeners(AuditListener.class)
@Table(name = "CUSTOMER_ACCOUNT", uniqueConstraints = @UniqueConstraint(name = "UK_CUSTOMER_STORE_SUB",
        columnNames = {"STORE_MERCHANT_ID", "CUA_EXTERNAL_ID"}))
@Getter
@Setter
public class Customer extends SalesManagerEntity<Long, Customer> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "CUSTOMER_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT", pkColumnValue = "CUSTOMER_ACCOUNT_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @Embedded
    @AttributeOverride(name = "storeMerchantId",
            column = @Column(name = "STORE_MERCHANT_ID", nullable = false, length = 50))
    private StoreMerchantId storeMerchantId;

    @Column(name = "CUA_EXTERNAL_ID", nullable = false, length = 96)
    private String cuaExternalId;

    @Column(name = "EMAIL", nullable = false, length = 96)
    private String email;

    @Column(name = "FIRST_NAME", length = 64)
    private String firstName;

    @Column(name = "LAST_NAME", length = 64)
    private String lastName;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "BILLING_FIRST_NAME", length = 64)),
            @AttributeOverride(name = "lastName", column = @Column(name = "BILLING_LAST_NAME", length = 64)),
            @AttributeOverride(name = "company", column = @Column(name = "BILLING_COMPANY", length = 100)),
            @AttributeOverride(name = "streetAddress", column = @Column(name = "BILLING_STREET_ADDRESS", length = 256)),
            @AttributeOverride(name = "city", column = @Column(name = "BILLING_CITY", length = 100)),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "BILLING_STATE", length = 100)),
            @AttributeOverride(name = "postcode", column = @Column(name = "BILLING_POSTCODE", length = 20)),
            @AttributeOverride(name = "telephone", column = @Column(name = "BILLING_TELEPHONE", length = 32)),
            @AttributeOverride(name = "country", column = @Column(name = "BILLING_COUNTRY_CODE", length = 6)),
            @AttributeOverride(name = "zoneCode", column = @Column(name = "BILLING_ZONE_CODE", length = 100))})
    private AddressSnapshot billing = new AddressSnapshot();

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "firstName", column = @Column(name = "DELIVERY_FIRST_NAME", length = 64)),
            @AttributeOverride(name = "lastName", column = @Column(name = "DELIVERY_LAST_NAME", length = 64)),
            @AttributeOverride(name = "company", column = @Column(name = "DELIVERY_COMPANY", length = 100)),
            @AttributeOverride(name = "streetAddress", column = @Column(name = "DELIVERY_STREET_ADDRESS", length = 256)),
            @AttributeOverride(name = "city", column = @Column(name = "DELIVERY_CITY", length = 100)),
            @AttributeOverride(name = "stateProvince", column = @Column(name = "DELIVERY_STATE", length = 100)),
            @AttributeOverride(name = "postcode", column = @Column(name = "DELIVERY_POSTCODE", length = 20)),
            @AttributeOverride(name = "telephone", column = @Column(name = "DELIVERY_TELEPHONE", length = 32)),
            @AttributeOverride(name = "country", column = @Column(name = "DELIVERY_COUNTRY_CODE", length = 6)),
            @AttributeOverride(name = "zoneCode", column = @Column(name = "DELIVERY_ZONE_CODE", length = 100))})
    private AddressSnapshot delivery = new AddressSnapshot();

    public Customer() {
    }

    public Customer(StoreMerchantId storeMerchantId, String cuaExternalId, String email) {
        this.storeMerchantId = storeMerchantId;
        this.cuaExternalId = cuaExternalId;
        this.email = email;
    }
}
