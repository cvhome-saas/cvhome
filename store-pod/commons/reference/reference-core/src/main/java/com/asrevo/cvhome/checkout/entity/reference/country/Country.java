package com.asrevo.cvhome.checkout.entity.reference.country;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "COUNTRY")
@Cacheable
@Getter
@Setter
public class Country extends SalesManagerEntity<CountryIsoCode, Country> {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonSerialize(using = CountryIsoCodeSerializer.class)
    @JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
    @AttributeOverride(name = "isoCode", column = @Column(name = "COUNTRY_ISOCODE", length = 6))
    private CountryIsoCode isoCode;

    @JsonIgnore
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private Set<CountryDescription> descriptions = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "country")
    private Set<Zone> zones = new HashSet<>();

    @Column(name = "COUNTRY_SUPPORTED")
    private boolean supported = true;

    @Transient
    private String name;

    public Country() {
    }

    @Override
    public CountryIsoCode getId() {
        return isoCode;
    }

    @Override
    public void setId(CountryIsoCode id) {
        this.isoCode = id;
    }

}
