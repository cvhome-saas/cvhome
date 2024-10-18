package com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer;

import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.Description;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "MANUFACTURER_DESCRIPTION",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"MANUFACTURER_ID", "LANGUAGE_ID"})})
@TableGenerator(
        name = "description_gen",
        table = "SM_SEQUENCER",
        pkColumnName = "SEQ_NAME",
        valueColumnName = "SEQ_COUNT",
        pkColumnValue = "manufacturer_description_seq",
        allocationSize = SchemaConstant.DESCRIPTION_ID_ALLOCATION_SIZE,
        initialValue = SchemaConstant.DESCRIPTION_ID_START_VALUE)
@Getter
@Setter
public class ManufacturerDescription extends Description {
    @Serial private static final long serialVersionUID = 1L;

    @JsonIgnore
    @ManyToOne(targetEntity = Manufacturer.class)
    @JoinColumn(name = "MANUFACTURER_ID", nullable = false)
    private Manufacturer manufacturer;

    @Column(name = "MANUFACTURERS_URL")
    private String url;

    @Column(name = "URL_CLICKED")
    private Integer urlClicked;

    @Column(name = "DATE_LAST_CLICK")
    private Date dateLastClick;

    public ManufacturerDescription() {}
}
