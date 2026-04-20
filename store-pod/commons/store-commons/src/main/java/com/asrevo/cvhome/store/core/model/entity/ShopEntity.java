package com.asrevo.cvhome.store.core.model.entity;

import java.io.Serial;
import java.io.Serializable;

import com.asrevo.cvhome.commons.domain.Entity;
import com.asrevo.cvhome.store.core.model.reference.LanguageCode;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class ShopEntity extends Entity implements Serializable {

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = LanguageCodeSerializer.class)
    @JsonDeserialize(using = LanguageCodeDeSerializer.class)
    private LanguageCode language;

}
