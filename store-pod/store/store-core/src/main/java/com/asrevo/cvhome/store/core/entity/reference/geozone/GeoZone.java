package com.asrevo.cvhome.store.core.entity.reference.geozone;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "GEOZONE")
// TODO : create DAO / Service
@Getter
@Setter
public class GeoZone extends SalesManagerEntity<Long, GeoZone> {
    @Serial
    private static final long serialVersionUID = -5992008645857938825L;

    @Id
    @Column(name = "GEOZONE_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "GEOZONE_SEQ_NEXT_VAL", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @OneToMany(mappedBy = "geoZone", cascade = CascadeType.ALL)
    private List<GeoZoneDescription> descriptions = new ArrayList<GeoZoneDescription>();

    @JsonIgnore
    @OneToMany(mappedBy = "geoZone", targetEntity = Country.class)
    private List<Country> countries = new ArrayList<Country>();


    @Column(name = "GEOZONE_NAME")
    private String name;

    @Column(name = "GEOZONE_CODE")
    private String code;

    public GeoZone() {
    }


}
