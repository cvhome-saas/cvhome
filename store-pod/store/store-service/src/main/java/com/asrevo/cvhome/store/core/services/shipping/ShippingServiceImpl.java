package com.asrevo.cvhome.store.core.services.shipping;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfiguration;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.shipping.ShippingConfiguration;
import com.asrevo.cvhome.store.core.model.shipping.ShippingType;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.system.MerchantConfigurationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ShippingServiceImpl implements ShippingService {
    private final static String SUPPORTED_COUNTRIES = "SUPPORTED_CNTR";
    private final static String SHIPPING_MODULES = "SHIPPING";
    private final static String SHIPPING_DISTANCE = "shippingDistanceModule";

    private final CountryService countryService;
    private final MerchantConfigurationService merchantConfigurationService;

    public ShippingServiceImpl(CountryService countryService, MerchantConfigurationService merchantConfigurationService) {
        this.countryService = countryService;
        this.merchantConfigurationService = merchantConfigurationService;
    }

    @Override
    public List<Country> getShipToCountryList(MerchantStore store, Language language) throws ServiceException {


        ShippingConfiguration shippingConfiguration = getShippingConfiguration(store);
        ShippingType shippingType = ShippingType.INTERNATIONAL;
        List<String> supportedCountries = new ArrayList<String>();
        if (shippingConfiguration == null) {
            shippingConfiguration = new ShippingConfiguration();
        }

        if (shippingConfiguration.getShippingType() != null) {
            shippingType = shippingConfiguration.getShippingType();
        }


        if (shippingType.name().equals(ShippingType.NATIONAL.name())) {

            supportedCountries.add(store.getCountry().getIsoCode());

        } else {

            MerchantConfiguration configuration = merchantConfigurationService.getMerchantConfiguration(SUPPORTED_COUNTRIES, store);

            if (configuration != null) {

                String countries = configuration.getValue();
                if (!StringUtils.isBlank(countries)) {

                    Object objRegions = JSONValue.parse(countries);
                    JSONArray arrayRegions = (JSONArray) objRegions;
                    for (Object arrayRegion : arrayRegions) {
                        supportedCountries.add((String) arrayRegion);
                    }
                }

            }

        }

        return countryService.getCountries(supportedCountries, language);

    }

    @Override
    public ShippingConfiguration getShippingConfiguration(MerchantStore store) throws ServiceException {

        MerchantConfiguration configuration = merchantConfigurationService.getMerchantConfiguration(Constants.SHIPPING_CONFIGURATION, store);

        ShippingConfiguration shippingConfiguration = null;

        if (configuration != null) {
            String value = configuration.getValue();

            ObjectMapper mapper = new ObjectMapper();
            try {
                shippingConfiguration = mapper.readValue(value, ShippingConfiguration.class);
            } catch (Exception e) {
                throw new ServiceException("Cannot parse json string " + value);
            }
        }
        return shippingConfiguration;

    }

    @Override
    public boolean hasTaxOnShipping(MerchantStore store) throws ServiceException {
        ShippingConfiguration shippingConfiguration = getShippingConfiguration(store);
        return shippingConfiguration.isTaxOnShipping();
    }
}
