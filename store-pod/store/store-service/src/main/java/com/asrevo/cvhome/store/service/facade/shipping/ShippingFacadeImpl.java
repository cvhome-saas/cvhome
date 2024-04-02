package com.asrevo.cvhome.store.service.facade.shipping;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.controller.exception.ConversionRuntimeException;
import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.references.ReadableCountry;
import com.asrevo.cvhome.store.core.services.shipping.ShippingService;
import com.asrevo.cvhome.store.service.populator.references.ReadableCountryPopulator;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service("shippingFacade")
public class ShippingFacadeImpl implements ShippingFacade {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShippingFacadeImpl.class);


    @Autowired
    ShippingService shippingService;


    @Override
    public List<ReadableCountry> shipToCountry(MerchantStore store, Language language) {

        try {
            List<Country> countries = shippingService.getShipToCountryList(store, language);

            List<ReadableCountry> countryList = new ArrayList<ReadableCountry>();

            if (!CollectionUtils.isEmpty(countries)) {

                countryList = countries.stream()
                        .map(c -> {
                            try {
                                return convert(c, store, language);
                            } catch (ConversionException e) {
                                throw new ConversionRuntimeException("Error converting Country to readable country,e");
                            }
                        })
                        .sorted(Comparator.comparing(ReadableCountry::getName))
                        .collect(Collectors.toList());

            }

            return countryList;
        } catch (Exception e) {
            throw new ServiceRuntimeException("Error getting shipping country", e);
        }


    }

    ReadableCountry convert(Country country, MerchantStore store, Language lang) throws ConversionException {
        ReadableCountryPopulator countryPopulator = new ReadableCountryPopulator();
        return countryPopulator.populate(country, store, lang);
    }

}
