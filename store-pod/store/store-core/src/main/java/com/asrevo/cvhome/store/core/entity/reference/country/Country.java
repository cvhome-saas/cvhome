package com.asrevo.cvhome.store.core.entity.reference.country;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.reference.geozone.GeoZone;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COUNTRY")
@Cacheable
@Getter
@Setter
public class Country extends SalesManagerEntity<Integer, Country> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "COUNTRY_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "COUNTRY_SEQ_NEXT_VAL")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Integer id;

    @JsonIgnore
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private Set<CountryDescription> descriptions = new HashSet<CountryDescription>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "country")
    private Set<Zone> zones = new HashSet<Zone>();

    @ManyToOne(targetEntity = GeoZone.class)
    @JoinColumn(name = "GEOZONE_ID")
    private GeoZone geoZone;

    @Column(name = "COUNTRY_SUPPORTED")
    private boolean supported = true;

    @Column(name = "COUNTRY_ISOCODE", unique = true, nullable = false)
    private String isoCode;

    @Transient
    private String name;

    public Country() {
    }

    public Country(String isoCode) {
        this.setIsoCode(isoCode);
    }

}
