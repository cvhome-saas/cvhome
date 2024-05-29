package com.asrevo.cvhome.store.core.entity.reference.country;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.reference.geozone.GeoZone;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "COUNTRY")
@Cacheable
@Getter
@Setter
public class Country extends SalesManagerEntity<Integer, Country> {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "COUNTRY_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT",
            pkColumnValue = "COUNTRY_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Integer id;

    @JsonIgnore
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
    private Set<CountryDescription> descriptions = new HashSet<>();

    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "country")
    private Set<Zone> zones = new HashSet<>();

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
