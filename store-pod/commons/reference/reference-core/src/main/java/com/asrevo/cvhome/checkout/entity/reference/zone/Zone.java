package com.asrevo.cvhome.checkout.entity.reference.zone;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "ZONE")
@Getter
@Setter
public class Zone extends SalesManagerEntity<ZoneCode, Zone> {

    @Serial
    private static final long serialVersionUID = 1L;

    @EmbeddedId
    @JsonSerialize(using = ZoneCodeSerializer.class)
    @JsonDeserialize(using = ZoneCodeDeSerializer.class)
    @AttributeOverride(name = "code", column = @Column(name = "zone_code", length = 100))
    private ZoneCode code;

    @JsonIgnore
    @OneToMany(mappedBy = "zone", cascade = CascadeType.ALL)
    private List<ZoneDescription> descriptions = new ArrayList<>();

    @JsonIgnore
    @Column(name = "COUNTRY_ID", length = 6)
    @Convert(converter = CountryIsoCodeConverter.class)
    private CountryIsoCode country;

    @Transient
    private String name;

    @Override
    public ZoneCode getId() {
        return code;
    }

    @Override
    public void setId(ZoneCode id) {
        this.code = id;
    }

}
