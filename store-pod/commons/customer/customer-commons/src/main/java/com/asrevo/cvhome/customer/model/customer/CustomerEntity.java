package com.asrevo.cvhome.customer.model.customer;

import com.asrevo.cvhome.customer.model.customer.address.Address;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Setter
@Getter
public class CustomerEntity extends Customer implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	@Email(message = "{messages.invalid.email}")
	@NotEmpty(message = "{NotEmpty.customer.emailAddress}")
	private String emailAddress;

	@Valid
	private Address billing;

	private Address delivery;

	private String gender;

	@JsonSerialize(using = LanguageCodeSerializer.class)
	@JsonDeserialize(using = LanguageCodeDeSerializer.class)
	private LanguageCode language;

	private String firstName;

	private String lastName;

	private String provider; // online, facebook ...

	private String storeCode;

	// @NotEmpty(message="{NotEmpty.customer.userName}")
	// can be email or anything else
	private String userName;

	private Double rating = 0D;

	private int ratingCount;

}
