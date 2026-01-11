package com.asrevo.cvhome.checkout.entity.reference.country;

import com.asrevo.cvhome.checkout.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.io.Serial;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "COUNTRY")
@Cacheable
@Getter
@Setter
public class Country extends SalesManagerEntity<CountryIsoCode, Country> {

	@Serial
	private static final long serialVersionUID = 1L;

	@EmbeddedId
	@JsonSerialize(using = CountryIsoCodeSerializer.class)
	@JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
	@AttributeOverrides({
			@AttributeOverride(name = "isoCode", column = @Column(name = "COUNTRY_ISOCODE", length = 6)) })
	private CountryIsoCode isoCode;

	@JsonIgnore
	@OneToMany(mappedBy = "country", cascade = CascadeType.ALL)
	private Set<CountryDescription> descriptions = new HashSet<>();

	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "country")
	private Set<Zone> zones = new HashSet<>();

	@Column(name = "COUNTRY_SUPPORTED")
	private boolean supported = true;

	@Transient
	private String name;

	public Country() {
	}

	@Override
	public CountryIsoCode getId() {
		return isoCode;
	}

	@Override
	public void setId(CountryIsoCode id) {
		this.isoCode = id;
	}

}
