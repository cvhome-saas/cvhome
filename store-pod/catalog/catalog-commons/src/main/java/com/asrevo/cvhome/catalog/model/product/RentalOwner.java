package com.asrevo.cvhome.catalog.model.product;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.customer.model.customer.address.Address;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

/**
 * RENTAL customer
 *
 * @author c.samson
 */
@Setter
@Getter
public class RentalOwner extends Entity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	private String firstName;

	private String lastName;

	private Address address;

	private String emailAddress;

}
