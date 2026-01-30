package com.asrevo.cvhome.store.model.references;

import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Setter
@Getter
public class ReadableLanguage implements Serializable {

	/**
	 *
	 */
	@Serial
	private static final long serialVersionUID = 1L;

	@JsonSerialize(using = LanguageCodeSerializer.class)
	@JsonDeserialize(using = LanguageCodeDeSerializer.class)
	private LanguageCode code;

	@JsonSerialize(using = LanguageCodeSerializer.class)
	@JsonDeserialize(using = LanguageCodeDeSerializer.class)
	private LanguageCode id;

}
