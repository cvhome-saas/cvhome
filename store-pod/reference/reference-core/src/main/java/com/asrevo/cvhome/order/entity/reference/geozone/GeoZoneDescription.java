package com.asrevo.cvhome.order.entity.reference.geozone;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "GEOZONE_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"GEOZONE_ID", "LANGUAGE_CODE"})})
@TableGenerator(
        name = "description_gen",
        table = "SM_SEQUENCER",
        pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT",
        pkColumnValue = "GEOZONE_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class GeoZoneDescription extends Description {
    @Serial private static final long serialVersionUID = 1L;

    @ManyToOne(targetEntity = GeoZone.class)
    @JoinColumn(name = "GEOZONE_ID")
    private GeoZone geoZone;
}
