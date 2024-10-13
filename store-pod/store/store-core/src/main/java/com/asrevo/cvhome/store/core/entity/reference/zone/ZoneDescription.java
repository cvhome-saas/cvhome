package com.asrevo.cvhome.store.core.entity.reference.zone;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "ZONE_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"ZONE_ID", "LANGUAGE_ID"})})
@TableGenerator(
        name = "description_gen",
        table = "SM_SEQUENCER",
        pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT",
        pkColumnValue = "zone_description_seq",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
// @SequenceGenerator(name = "description_gen", sequenceName = "zone_description_seq",
// allocationSize = SchemaConstant.DESCRIPTION_ID_SEQUENCE_START)
@Getter
@Setter
public class ZoneDescription extends Description {
    @Serial private static final long serialVersionUID = 1L;

    @JsonIgnore
    @ManyToOne(targetEntity = Zone.class)
    @JoinColumn(name = "ZONE_ID", nullable = false)
    private Zone zone;

    public ZoneDescription() {}

    public ZoneDescription(Zone zone, Language language, String name) {
        setZone(zone);
        setLanguage(language);
        setName(name);
    }
}
