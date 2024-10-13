package com.asrevo.cvhome.store.core.model.shipping;

import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

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
