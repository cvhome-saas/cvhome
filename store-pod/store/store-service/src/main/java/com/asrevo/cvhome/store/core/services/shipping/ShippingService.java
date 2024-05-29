package com.asrevo.cvhome.store.core.services.shipping;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.shipping.ShippingConfiguration;

import java.util.List;

/*@TODO ASHRAF*/
public interface ShippingService {


    /**
     * Returns a list of supported countries (ship to country list) configured by merchant
     * If the merchant configured shipping National, then only store country will be in the list
     * If the merchant configured shipping International, then the list of accepted country is returned
     * from the list
     *
     */
    List<Country> getShipToCountryList(MerchantStore store, Language language)
            throws ServiceException;

    ShippingConfiguration getShippingConfiguration(MerchantStore store) throws ServiceException;

    boolean hasTaxOnShipping(MerchantStore store) throws ServiceException;


}