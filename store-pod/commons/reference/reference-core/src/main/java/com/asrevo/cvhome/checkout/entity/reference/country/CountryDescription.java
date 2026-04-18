package com.asrevo.cvhome.checkout.entity.reference.country;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.TableGenerator;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "COUNTRY_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"COUNTRY_ID", "LANGUAGE_CODE"})})
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT", pkColumnValue = "COUNTRY_DESCRIPTION_SEQ_NEXT_VAL",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class CountryDescription extends Description {

    @Serial
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @Column(name = "COUNTRY_ID", length = 6)
    @Convert(converter = CountryIsoCodeConverter.class)
    private CountryIsoCode country;

    public CountryDescription() {
    }

}
