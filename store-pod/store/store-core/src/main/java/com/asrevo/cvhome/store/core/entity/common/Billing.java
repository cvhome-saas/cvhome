package com.asrevo.cvhome.store.core.entity.common;

import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class Billing {

    @NotEmpty
    @Column(name = "BILLING_LAST_NAME", length = 64, nullable = false)
    private String lastName;

    @NotEmpty
    @Column(name = "BILLING_FIRST_NAME", length = 64, nullable = false)
    private String firstName;

    @Column(name = "BILLING_COMPANY", length = 100)
    private String company;

    @Column(name = "BILLING_STREET_ADDRESS", length = 256)
    private String address;

    @Column(name = "BILLING_CITY", length = 100)
    private String city;

    @Column(name = "BILLING_POSTCODE", length = 20)
    private String postalCode;

    @Column(name = "BILLING_TELEPHONE", length = 32)
    private String telephone;

    @Column(name = "BILLING_STATE", length = 100)
    private String state;

    @Column(name = "LONGITUDE", length = 100)
    private String longitude;

    @Column(name = "LATITUDE", length = 100)
    private String latitude;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Country.class)
    @JoinColumn(name = "BILLING_COUNTRY_ID", nullable = false)
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Zone.class)
    @JoinColumn(name = "BILLING_ZONE_ID")
    private Zone zone;
}
