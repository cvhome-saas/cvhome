package com.asrevo.cvhome.store.core.entity.reference.zone;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ZONE")
@Getter
@Setter
public class Zone extends SalesManagerEntity<Long, Zone> {
    @Serial private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ZONE_ID")
    @TableGenerator(
            name = "TABLE_GEN",
            table = "SM_SEQUENCER",
            pkColumnName = "SEQ_NAME",
            valueColumnName = "SEQ_COUNT",
            pkColumnValue = "ZONE_SEQ_NEXT_VAL",
            allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
            initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @JsonIgnore
    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    private List<ZoneDescription> descriptions = new ArrayList<>();

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COUNTRY_ID", nullable = false)
    private Country country;

    @Transient private String name;

    @Column(name = "ZONE_CODE", unique = true, nullable = false)
    private String code;

    public Zone() {}

    public Zone(Country country, String name, String code) {
        this.setCode(code);
        this.setCountry(country);
        this.setCode(name);
    }
}
