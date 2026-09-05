package com.asrevo.cvhome.checkout.entity;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;

import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;

import lombok.Getter;
import lombok.Setter;

/**
 * A postal address as it was at the time it was written — on a customer as their current one, on an order as the
 * one the order ships to. Embedded twice on each with a {@code BILLING_} / {@code DELIVERY_} prefix.
 */
@Embeddable
@Getter
@Setter
public class AddressSnapshot implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Column(name = "FIRST_NAME", length = 64)
    private String firstName;

    @Column(name = "LAST_NAME", length = 64)
    private String lastName;

    @Column(name = "COMPANY", length = 100)
    private String company;

    @Column(name = "STREET_ADDRESS", length = 256)
    private String streetAddress;

    @Column(name = "CITY", length = 100)
    private String city;

    @Column(name = "STATE", length = 100)
    private String stateProvince;

    @Column(name = "POSTCODE", length = 20)
    private String postcode;

    @Column(name = "TELEPHONE", length = 32)
    private String telephone;

    @Column(name = "COUNTRY_CODE", length = 6)
    @Convert(converter = CountryIsoCodeConverter.class)
    private CountryIsoCode country;

    @Column(name = "ZONE_CODE", length = 100)
    private String zoneCode;

    public boolean isEmpty() {
        boolean noName = firstName == null && lastName == null;
        boolean noPlace = streetAddress == null && city == null;
        return noName && noPlace && country == null;
    }
}
