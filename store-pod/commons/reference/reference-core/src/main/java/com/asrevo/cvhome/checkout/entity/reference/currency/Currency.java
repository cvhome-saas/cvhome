package com.asrevo.cvhome.checkout.entity.reference.currency;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;
import jakarta.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Entity
@Table(name = "CURRENCY")
@Cacheable
@Getter
@Setter
public class Currency extends SalesManagerEntity<CurrencyCode, Currency> implements Serializable {

	@Serial
	private static final long serialVersionUID = -999926410367685145L;

	@EmbeddedId
	@JsonSerialize(using = LanguageCodeSerializer.class)
	@JsonDeserialize(using = LanguageCodeDeSerializer.class)
	@AttributeOverrides({ @AttributeOverride(name = "code", column = @Column(name = "CURRENCY_CODE", length = 6)) })
	private CurrencyCode code;

	@Column(name = "CURRENCY_SUPPORTED")
	private Boolean supported = true;

	@Column(name = "CURRENCY_NAME", unique = true)
	private String name;

	public Currency() {
	}

	@Override
	public CurrencyCode getId() {
		return this.code;
	}

	@Override
	public void setId(CurrencyCode id) {
		this.code = id;
	}

}
