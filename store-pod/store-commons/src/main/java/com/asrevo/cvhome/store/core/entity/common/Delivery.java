package com.asrevo.cvhome.store.core.entity.common;

import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;
import com.asrevo.cvhome.store.core.converter.ZoneCodeConverter;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class Delivery {

    @Column(name = "DELIVERY_LAST_NAME", length = 64)
    private String lastName;

    @Column(name = "DELIVERY_FIRST_NAME", length = 64)
    private String firstName;

    @Column(name = "DELIVERY_COMPANY", length = 100)
    private String company;

    @Column(name = "DELIVERY_STREET_ADDRESS", length = 256)
    private String address;

    @Column(name = "DELIVERY_CITY", length = 100)
    private String city;

    @Column(name = "DELIVERY_POSTCODE", length = 20)
    private String postalCode;

    @Column(name = "DELIVERY_STATE", length = 100)
    private String state;

    @Column(name = "DELIVERY_TELEPHONE", length = 32)
    private String telephone;

    @Column(name = "DELIVERY_COUNTRY_CODE", length = 6)
    @Convert(converter = CountryIsoCodeConverter.class)
    private CountryIsoCode country;

    @Column(name = "DELIVERY_ZONE_ID", length = 100)
    @Convert(converter = ZoneCodeConverter.class)
    private ZoneCode zone;

    @Transient private String latitude = null;

    @Transient private String longitude = null;
}
