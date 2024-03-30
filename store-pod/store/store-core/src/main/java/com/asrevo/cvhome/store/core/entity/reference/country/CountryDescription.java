package com.asrevo.cvhome.store.core.entity.reference.country;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "COUNTRY_DESCRIPTION", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "COUNTRY_ID",
                "LANGUAGE_ID"
        })
}
)
@TableGenerator(name = "description_gen", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "country_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE, initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
//@SequenceGenerator(name = "description_gen", sequenceName = "country_description_seq", allocationSize = SchemaConstant.DESCRIPTION_ID_SEQUENCE_START)
@Getter
@Setter
public class CountryDescription extends Description {
    private static final long serialVersionUID = 1L;

    @JsonIgnore
    @ManyToOne(targetEntity = Country.class)
    @JoinColumn(name = "COUNTRY_ID", nullable = false)
    private Country country;

    public CountryDescription() {
    }

    public CountryDescription(Language language, String name) {
        this.setLanguage(language);
        this.setName(name);
    }

}
