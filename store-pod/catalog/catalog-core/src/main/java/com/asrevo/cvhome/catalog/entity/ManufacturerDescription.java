package com.asrevo.cvhome.catalog.entity;

import java.io.Serial;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.commons.domain.StoreScoped;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.common.description.BaseDescription;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "MANUFACTURER_DESCRIPTION",
        uniqueConstraints = @UniqueConstraint(columnNames = {"MANUFACTURER_ID", "LANGUAGE_CODE"}))
@SequenceGenerator(name = "description_gen", sequenceName = "manufacturer_description_seq",
        allocationSize = SchemaConstant.ID_ALLOCATION_SIZE)
@Getter
@Setter
public class ManufacturerDescription extends BaseDescription implements StoreScoped {

    @Serial
    private static final long serialVersionUID = 1L;

    @ManyToOne
    @JoinColumn(name = "MANUFACTURER_ID", nullable = false)
    private Manufacturer manufacturer;

    @Column(name = "MANUFACTURERS_URL")
    private String url;

    public ManufacturerDescription() {
    }

    public ManufacturerDescription(Manufacturer manufacturer) {
        this.manufacturer = manufacturer;
    }

    @Override
    public StoreMerchantId scopedStore() {
        return manufacturer == null ? null : manufacturer.scopedStore();
    }
}
