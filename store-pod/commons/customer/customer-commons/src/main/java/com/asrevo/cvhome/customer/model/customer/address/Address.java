package com.asrevo.cvhome.customer.model.customer.address;

import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * Customer or someone address
 *
 * @author carlsamson
 */
@Setter
@Getter
public class Address extends AddressLocation implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	// @NotEmpty(message="{NotEmpty.customer.firstName}")
	private String firstName;

	// @NotEmpty(message="{NotEmpty.customer.lastName}")
	private String lastName;

	private String bilstateOther;

	private String company;

	private String phone;

	private String address;

	private String city;

	private String stateProvince;

	private boolean billingAddress;

	private String latitude;

	private String longitude;

	@JsonSerialize(using = ZoneCodeSerializer.class)
	@JsonDeserialize(using = ZoneCodeDeSerializer.class)
	private ZoneCode zone; // code

	// @NotEmpty(message="{NotEmpty.customer.billing.country}")
	@JsonSerialize(using = CountryIsoCodeSerializer.class)
	@JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
	private CountryIsoCode country;

}
