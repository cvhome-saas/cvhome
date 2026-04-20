package com.asrevo.cvhome.checkout.entity.reference.zone;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.ZoneCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ZONE_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"ZONE_ID", "LANGUAGE_CODE"})})
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "ZONE_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ZoneDescription extends BaseDescription {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Column(name = "ZONE_ID", nullable = false, length = 100)
    @Convert(converter = ZoneCodeConverter.class)
    private ZoneCode zone;

}
