package com.asrevo.cvhome.store.model.references;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.ZoneCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.ZoneCodeSerializer;
import java.io.Serial;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Setter
@Getter
public class ZoneEntity extends Entity {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	@JsonSerialize(using = CountryIsoCodeSerializer.class)
	@JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
	private CountryIsoCode countryCode;

	@JsonSerialize(using = ZoneCodeSerializer.class)
	@JsonDeserialize(using = ZoneCodeDeSerializer.class)
	private ZoneCode code;

}
