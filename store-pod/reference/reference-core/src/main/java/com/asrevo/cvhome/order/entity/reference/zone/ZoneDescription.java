package com.asrevo.cvhome.order.entity.reference.zone;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.ZoneCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "ZONE_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"ZONE_ID", "LANGUAGE_CODE"})})
@TableGenerator(
        name = "description_gen",
        table = "SM_SEQUENCER",
        pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT",
        pkColumnValue = "ZONE_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ZoneDescription extends Description {
    @Serial private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Column(name = "ZONE_ID", nullable = false, length = 100)
    @Convert(converter = ZoneCodeConverter.class)
    private ZoneCode zone;

    public ZoneDescription() {}
}
