package com.asrevo.cvhome.store.core.entity.common;

import com.asrevo.cvhome.store.core.converter.CountryIsoCodeConverter;
import com.asrevo.cvhome.store.core.converter.ZoneCodeConverter;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Embeddable
public class Billing {

	@NotEmpty
	@Column(name = "BILLING_LAST_NAME", length = 64, nullable = false)
	private String lastName;

	@NotEmpty
	@Column(name = "BILLING_FIRST_NAME", length = 64, nullable = false)
	private String firstName;

	@Column(name = "BILLING_COMPANY", length = 100)
	private String company;

	@Column(name = "BILLING_STREET_ADDRESS", length = 256)
	private String address;

	@Column(name = "BILLING_CITY", length = 100)
	private String city;

	@Column(name = "BILLING_POSTCODE", length = 20)
	private String postalCode;

	@Column(name = "BILLING_TELEPHONE", length = 32)
	private String telephone;

	@Column(name = "BILLING_STATE", length = 100)
	private String state;

	@JsonIgnore
	@Column(name = "BILLING_COUNTRY_CODE", length = 6)
	@Convert(converter = CountryIsoCodeConverter.class)
	private CountryIsoCode country;

	@JsonIgnore
	@Column(name = "BILLING_ZONE_CODE", length = 6)
	@Convert(converter = ZoneCodeConverter.class)
	private ZoneCode zone;

}
