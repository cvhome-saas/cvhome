package com.asrevo.cvhome.store.core.model.shipping;

import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


/**
 * Describes how shipping is configured for a given store
 *
 * @author carlsamson
 */
@Getter
@Setter
public class ShippingMetaData {

    private List<String> modules;
    private List<String> preProcessors;
    private List<String> postProcessors;
    private List<Country> shipToCountry;
    private boolean useDistanceModule;
    private boolean useAddressAutoComplete;


}
