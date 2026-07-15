package com.asrevo.cvhome.store.core.model;

import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CountryIsoCodeSerializer;
import com.asrevo.cvhome.store.core.serializer.CurrencyCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.CurrencyCodeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeDeSerializer;
import com.asrevo.cvhome.store.core.serializer.LanguageCodeSerializer;

import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MerchantStorePricingBase {

    private boolean currencyFormatNational;

    @JsonSerialize(using = CurrencyCodeSerializer.class)
    @JsonDeserialize(using = CurrencyCodeDeSerializer.class)
    private CurrencyCode currency; // code

    @JsonSerialize(using = LanguageCodeSerializer.class)
    @JsonDeserialize(using = LanguageCodeDeSerializer.class)
    private LanguageCode defaultLanguage; // code

    @JsonSerialize(using = CountryIsoCodeSerializer.class)
    @JsonDeserialize(using = CountryIsoCodeDeSerializer.class)
    private CountryIsoCode countryIsoCode;

}
