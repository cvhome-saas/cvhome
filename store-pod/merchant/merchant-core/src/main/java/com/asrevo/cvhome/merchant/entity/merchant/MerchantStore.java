package com.asrevo.cvhome.merchant.entity.merchant;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;
import com.asrevo.cvhome.store.core.converter.CurrencyCodeConverter;
import com.asrevo.cvhome.store.core.converter.LanguageCodeConverter;
import com.asrevo.cvhome.store.core.converter.ZoneCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.audit.AuditSection;
import com.asrevo.cvhome.store.core.entity.common.audit.Auditable;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.model.references.MeasureUnit;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.persistence.Entity;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MERCHANT_STORE", indexes = @Index(columnList = "LINEAGE"))
@Getter
@Setter
public class MerchantStore extends SalesManagerEntity<StoreMerchantId, MerchantStore>
        implements Auditable {

    @Serial private static final long serialVersionUID = 1L;

    @EmbeddedId
    @AttributeOverrides(
            value = {
                @AttributeOverride(
                        name = "storeMerchantId",
                        column = @Column(name = "STORE_MERCHANT_ID", length = 50))
            })
    private StoreMerchantId id;

    @Embedded private AuditSection auditSection = new AuditSection();

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

    @Temporal(TemporalType.DATE)
    @Column(name = "IN_BUSINESS_SINCE")
    private Date inBusinessSince = new Date();

    @Transient private String dateBusinessSince;

    @Column(name = "LANGUAGE_CODE", length = 6)
    @Convert(converter = LanguageCodeConverter.class)
    private LanguageCode defaultLanguageCode;

    @JsonIgnore
    @NotEmpty
    @ElementCollection(targetClass = LanguageCode.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "MERCHANT_LANGUAGE",
            joinColumns = {@JoinColumn(name = "STORE_MERCHANT_ID")})
    @AttributeOverrides(
            value = {
                @AttributeOverride(
                        name = "code",
                        column = @Column(name = "LANGUAGE_CODE", length = 6))
            })
    private List<LanguageCode> languages = new ArrayList<>();

    @JsonIgnore
    @ElementCollection(targetClass = SliderImage.class, fetch = FetchType.LAZY)
    @OrderColumn(name = "PRIORITY")
    @CollectionTable(
            name = "MERCHANT_SLIDER_IMAGES",
            joinColumns = {@JoinColumn(name = "STORE_MERCHANT_ID")})
    @AttributeOverrides(
            value = {
                @AttributeOverride(name = "priority", column = @Column(name = "PRIORITY")),
                @AttributeOverride(name = "name", column = @Column(name = "NAME", length = 100))
            })
    private Set<SliderImage> sliderImages = new HashSet<>();

    @JsonIgnore
    @ElementCollection(targetClass = SocialLink.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "SOCIAL_LINKS",
            joinColumns = {@JoinColumn(name = "STORE_MERCHANT_ID")})
    @AttributeOverrides(
            value = {
                @AttributeOverride(
                        name = "provider",
                        column = @Column(name = "PROVIDER", length = 10)),
                @AttributeOverride(name = "url", column = @Column(name = "URL", length = 100))
            })
    private Set<SocialLink> socialLinks = new HashSet<>();

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
    @Column(name = "STORE_BANNER", length = 100)
    private String storeBanner;

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

    public MerchantStore() {}
}
