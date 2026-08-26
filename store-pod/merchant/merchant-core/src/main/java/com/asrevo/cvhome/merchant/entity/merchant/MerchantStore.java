package com.asrevo.cvhome.merchant.entity.merchant;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

import com.asrevo.cvhome.commons.domain.ColorTheme;
import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.commons.domain.ManagerStoreDomain;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.Theme;
import com.asrevo.cvhome.commons.domain.ZoneCode;
import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;
import com.asrevo.cvhome.store.core.converter.CurrencyCodeConverter;
import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;
import com.asrevo.cvhome.store.core.converter.ZoneCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MERCHANT_STORE")
@Getter
@Setter
public class MerchantStore extends SalesManagerEntity<StoreMerchantId, MerchantStore> implements Auditable {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @AttributeOverride(name = "storeMerchantId", column = @Column(name = "STORE_MERCHANT_ID", length = 50))
    private StoreMerchantId id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @NotEmpty
    @Column(name = "STORE_NAME", nullable = false, length = 100)
    private String storename;

    @NotEmpty
    @Column(name = "ORG", nullable = false, updatable = false)
    private String org;

    @Column(name = "THEME", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private Theme theme;

    @Column(name = "COLOR_THEME", nullable = false, length = 25)
    @Enumerated(EnumType.STRING)
    private ColorTheme colorTheme;

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
    @Column(name = "COUNTRY_ID", length = 6)
    @Convert(converter = CountryIsoCodeConverter.class)
    private CountryIsoCode country;

    @JsonIgnore
    @Column(name = "ZONE_ID", length = 100)
    @Convert(converter = ZoneCodeConverter.class)
    private ZoneCode zone;

    @Column(name = "STORE_STATE_PROV", length = 100)
    @Convert(converter = ZoneCodeConverter.class)
    private ZoneCode storestateprovince;

    @Column(name = "WEIGHTUNITCODE", length = 5)
    private String weightunitcode = MeasureUnit.LB.name();

    @Column(name = "SEIZEUNITCODE", length = 5)
    private String seizeunitcode = MeasureUnit.IN.name();

    @Column(name = "IN_BUSINESS_SINCE")
    private LocalDate inBusinessSince = LocalDate.now();

    @Transient
    private String dateBusinessSince;

    @Column(name = "LANGUAGE_CODE", length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode defaultLanguageCode;

    @JsonIgnore
    @NotEmpty
    @ElementCollection(targetClass = LanguageCode.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "MERCHANT_LANGUAGE", joinColumns = {@JoinColumn(name = "STORE_MERCHANT_ID")})
    @AttributeOverride(name = "code", column = @Column(name = "LANGUAGE_CODE", length = 6))
    private List<LanguageCode> languages = new ArrayList<>();

    @JsonIgnore
    @ElementCollection(targetClass = ManagerStoreDomain.class, fetch = FetchType.LAZY)
    @CollectionTable(name = "STORE_DOMAINS", joinColumns = {@JoinColumn(name = "STORE_MERCHANT_ID")})
    @AttributeOverride(name = "domain", column = @Column(name = "DOMAIN", length = 100, unique = true))
    @AttributeOverride(name = "domainType", column = @Column(name = "DOMAIN_TYPE", length = 15))
    @Convert(attributeName = "domainType", converter = DomainTypeConverter.class)
    private Set<ManagerStoreDomain> storeDomains = new HashSet<>();

    @Column(name = "USE_CACHE")
    private boolean useCache = false;

    @Column(name = "REQUIRE_LOGIN_FOR_ORDER_PLACEMENT")
    private boolean requireLoginForOrderPlacement = false;

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
    @Column(name = "CURRENCY_ID", length = 6, nullable = false)
    @Convert(converter = CurrencyCodeConverter.class)
    private CurrencyCode currency;

    @Column(name = "CURRENCY_FORMAT_NATIONAL")
    private boolean currencyFormatNational;

    public MerchantStore(StoreMerchantId id, String name) {
        this.id = id;
        this.storename = name;
    }

    public MerchantStore(StoreMerchantId id, String name, String storeEmailAddress) {
        this.id = id;
        this.storename = name;
        this.storeEmailAddress = storeEmailAddress;
    }

    public MerchantStore() {
    }

}
