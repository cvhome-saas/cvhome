package com.asrevo.cvhome.store.core.services.geo;
//	@TODO ASHRAF

//import java.net.InetAddress;
//
//
//
//
//import com.maxmind.geoip2.DatabaseReader;
//import com.maxmind.geoip2.model.CityResponse;
//import com.asrevo.cvhome.store.core.exception.ServiceException;

import com.asrevo.cvhome.store.core.entity.common.Address;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Using Geolite2 City database
 * http://dev.maxmind.com/geoip/geoip2/geolite2/#Databases
 *
 * @author c.samson
 */
@Service
@Slf4j
public class GeoLocationImpl implements GeoLocation {

//	private DatabaseReader reader = null;

    //@TODO ASHRAF
    @Override
    public Address getAddress(String ipAddress) throws Exception {

//			if(reader==null) {
//					try {
//						java.io.InputStream inputFile = GeoLocationImpl.class.getClassLoader().getResourceAsStream("reference/GeoLite2-City.mmdb");
//						reader = new DatabaseReader.Builder(inputFile).build();
//					} catch(Exception e) {
//						LOGGER.error("Cannot instantiate IP database",e);
//					}
//			}
//
//			Address address = new Address();
//
//			try {
//
//			CityResponse response = reader.city(InetAddress.getByName(ipAddress));
//
//			address.setCountry(response.getCountry().getIsoCode());
//			address.setPostalCode(response.getPostal().getCode());
//			address.setZone(response.getMostSpecificSubdivision().getIsoCode());
//			address.setCity(response.getCity().getName());
//
//			} catch(com.maxmind.geoip2.exception.AddressNotFoundException ne) {
//				LOGGER.debug("Address not fount in DB " + ne.getMessage());
//			} catch(Exception e) {
//				throw new ServiceException(e);
//			}
//
//
//			return address;
        return null;

    }


}
