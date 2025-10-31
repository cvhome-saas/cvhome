package com.asrevo.cvhome.store.model.references;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CountryEntity extends Entity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	@JsonSerialize(using = CountryIsoCodeSerializer.class)
	@JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
	private CountryIsoCode code;

	private boolean supported;

}
