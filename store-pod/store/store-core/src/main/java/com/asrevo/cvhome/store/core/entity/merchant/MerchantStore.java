package com.asrevo.cvhome.store.core.entity.merchant;

import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.model.references.MeasureUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.*;

@Entity
@Table(name = "MERCHANT_STORE",
        indexes = @Index(columnList = "LINEAGE"))
@Getter
@Setter
public class MerchantStore extends SalesManagerEntity<Integer, MerchantStore> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "MERCHANT_ID", unique = true, nullable = false)
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "STORE_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Integer id;
    @Embedded
    private AuditSection auditSection = new AuditSection();
    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private MerchantStore parent;
    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE)
    private Set<MerchantStore> stores = new HashSet<MerchantStore>();
    @Column(name = "IS_RETAILER")
    private boolean retailer = false;
    @NotEmpty
    @Column(name = "STORE_NAME", nullable = false, length = 100)
    private String storename;
    @NotEmpty
    @Pattern(regexp = "^[a-zA-Z0-9_]*$")
    @Column(name = "STORE_CODE", nullable = false, unique = true, length = 100)
    private String code;
    @NotEmpty
    @Column(name = "ORG", nullable = false, updatable = false)
    private String org;
    @Column(name = "LINEAGE")
    private String lineage;
    @NotEmpty
    @Column(name = "STORE_PHONE", length = 50)
    private String storephone;
    @Column(name = "STORE_ADDRESS")
    private String storeaddress;
    @NotEmpty
    @Column(name = "STORE_CITY", length = 100)
    private String storecity;
    @NotEmpty
    @Column(name = "STORE_POSTAL_CODE", length = 15)
    private String storepostalcode;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Country.class)
    @JoinColumn(name = "COUNTRY_ID", nullable = false, updatable = true)
    private Country country;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Zone.class)
    @JoinColumn(name = "ZONE_ID", nullable = true, updatable = true)
    private Zone zone;
    @Column(name = "STORE_STATE_PROV", length = 100)
    private String storestateprovince;
    @Column(name = "WEIGHTUNITCODE", length = 5)
    private String weightunitcode = MeasureUnit.LB.name();
    @Column(name = "SEIZEUNITCODE", length = 5)
    private String seizeunitcode = MeasureUnit.IN.name();
    @Temporal(TemporalType.DATE)
    @Column(name = "IN_BUSINESS_SINCE")
    private Date inBusinessSince = new Date();
    @Transient
    private String dateBusinessSince;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Language.class)
    @JoinColumn(name = "LANGUAGE_ID", nullable = false)
    private Language defaultLanguage;
    @JsonIgnore
    @NotEmpty
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "MERCHANT_LANGUAGE")
    private List<Language> languages = new ArrayList<Language>();
    @Column(name = "USE_CACHE")
    private boolean useCache = false;
    @Column(name = "STORE_TEMPLATE", length = 25)
    private String storeTemplate;
    @Column(name = "INVOICE_TEMPLATE", length = 25)
    private String invoiceTemplate;
    @Column(name = "DOMAIN_NAME", length = 80)
    private String domainName;
    @JsonIgnore
    @Column(name = "CONTINUESHOPPINGURL", length = 150)
    private String continueshoppingurl;
    @Email
    @NotEmpty
    @Column(name = "STORE_EMAIL", length = 60, nullable = false)
    private String storeEmailAddress;
    @JsonIgnore
    @Column(name = "STORE_LOGO", length = 100)
    private String storeLogo;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Currency.class)
    @JoinColumn(name = "CURRENCY_ID", nullable = false)
    private Currency currency;
    @Column(name = "CURRENCY_FORMAT_NATIONAL")
    private boolean currencyFormatNational;

    public MerchantStore(Integer id, String code, String name) {
        this.id = id;
        this.code = code;
        this.storename = name;

    }

    public MerchantStore(Integer id, String code, String name, String storeEmailAddress) {
        this.id = id;
        this.code = code;
        this.storename = name;
        this.storeEmailAddress = storeEmailAddress;
    }

    public MerchantStore() {
    }
}
