package com.asrevo.cvhome.checkout.entity.reference.currency;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.asrevo.cvhome.store.core.entity.generic.SalesManagerEntity;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

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
    @AttributeOverride(name = "code", column = @Column(name = "CURRENCY_CODE", length = 6))
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
