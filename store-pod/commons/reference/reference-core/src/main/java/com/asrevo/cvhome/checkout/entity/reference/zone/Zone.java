package com.asrevo.cvhome.checkout.entity.reference.zone;

import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

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
	@AttributeOverrides({ @AttributeOverride(name = "code", column = @Column(name = "zone_code", length = 100)) })
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

	public Zone() {
	}

	@Override
	public ZoneCode getId() {
		return code;
	}

	@Override
	public void setId(ZoneCode id) {
		this.code = id;
	}

}
