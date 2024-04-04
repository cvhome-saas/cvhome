/**
 *
 */
package com.asrevo.cvhome.store.service.populator.customer;

import com.asrevo.cvhome.store.controller.exception.ConversionException;
import com.asrevo.cvhome.store.core.entity.common.Delivery;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.customer.ReadableDelivery;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.utils.AbstractDataPopulator;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * @author Carl Samson
 */
public class ReadableCustomerDeliveryAddressPopulator extends AbstractDataPopulator<Delivery, ReadableDelivery> {


    private CountryService countryService;
    private ZoneService zoneService;

    @Override
    public ReadableDelivery populate(Delivery source, ReadableDelivery target, MerchantStore store, Language language)
            throws ConversionException {


        if (countryService == null) {
            throw new ConversionException("countryService must be set");
        }

        if (zoneService == null) {
            throw new ConversionException("zoneService must be set");
        }


        target.setLatitude(source.getLatitude());
        target.setLongitude(source.getLongitude());


        if (StringUtils.isNotBlank(source.getCity())) {
            target.setCity(source.getCity());
        }

        if (StringUtils.isNotBlank(source.getCompany())) {
            target.setCompany(source.getCompany());
        }

        if (StringUtils.isNotBlank(source.getAddress())) {
            target.setAddress(source.getAddress());
        }

        if (StringUtils.isNotBlank(source.getFirstName())) {
            target.setFirstName(source.getFirstName());
        }

        if (StringUtils.isNotBlank(source.getLastName())) {
            target.setLastName(source.getLastName());
        }

        if (StringUtils.isNotBlank(source.getPostalCode())) {
            target.setPostalCode(source.getPostalCode());
        }

        if (StringUtils.isNotBlank(source.getTelephone())) {
            target.setPhone(source.getTelephone());
        }

        target.setStateProvince(source.getState());

        if (source.getTelephone() == null) {
            target.setPhone(source.getTelephone());
        }
        target.setAddress(source.getAddress());
        if (source.getCountry() != null) {
            target.setCountry(source.getCountry().getIsoCode());

            //resolve country name
            try {
                Map<String, Country> countries = countryService.getCountriesMap(language);
                Country c = countries.get(source.getCountry().getIsoCode());
                if (c != null) {
                    target.setCountryName(c.getName());
                }
            } catch (ServiceException e) {
                // TODO Auto-generated catch block
                throw new ConversionException(e);
            }
        }
        if (source.getZone() != null) {
            target.setZone(source.getZone().getCode());

            //resolve zone name
            try {
                Map<String, Zone> zones = zoneService.getZones(language);
                Zone z = zones.get(source.getZone().getCode());
                if (z != null) {
                    target.setProvinceName(z.getName());
                }
            } catch (ServiceException e) {
                // TODO Auto-generated catch block
                throw new ConversionException(e);
            }
        }


        return target;
    }

    @Override
    protected ReadableDelivery createTarget() {
        // TODO Auto-generated method stub
        return null;
    }

    public CountryService getCountryService() {
        return countryService;
    }

    public void setCountryService(CountryService countryService) {
        this.countryService = countryService;
    }

    public ZoneService getZoneService() {
        return zoneService;
    }

    public void setZoneService(ZoneService zoneService) {
        this.zoneService = zoneService;
    }


}
