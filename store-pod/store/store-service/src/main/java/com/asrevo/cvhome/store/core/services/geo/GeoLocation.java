package com.asrevo.cvhome.store.core.services.geo;

import com.asrevo.cvhome.store.core.entity.common.Address;

public interface GeoLocation {

    Address getAddress(String ipAddress) throws Exception;

}
