package com.asrevo.cvhome.store.core.entity.system;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

@Setter
@Getter
public class MerchantConfig implements Serializable, JSONAware {

    /**
     * TODO
     * Add a generic key value in order to allow the creation of configuration
     * on the fly from the client application and read from a key value map
     */
    @Serial private static final long serialVersionUID = 1L;

    private boolean displayCustomerSection = false;
    private boolean displayContactUs = false;
    private boolean displayStoreAddress = false;
    private boolean displayAddToCartOnFeaturedItems = false;
    private boolean displayCustomerAgreement = false;
    private boolean displayPagesMenu = true;
    private boolean allowPurchaseItems = true;
    private boolean displaySearchBox = true;
    private boolean testMode = false;
    private boolean debugMode = false;

    /**
     * Store default search json config
     **/
    private Map<String, Boolean> useDefaultSearchConfig =
            new HashMap<>(); // language code | true or false

    private Map<String, String> defaultSearchConfigPath =
            new HashMap<>(); // language code | file path

    @SuppressWarnings("unchecked")
    @Override
    public String toJSONString() {
        JSONObject data = new JSONObject();
        data.put("displayCustomerSection", this.isDisplayCustomerSection());
        data.put("displayContactUs", this.isDisplayContactUs());
        data.put("displayStoreAddress", this.isDisplayStoreAddress());
        data.put("displayAddToCartOnFeaturedItems", this.isDisplayAddToCartOnFeaturedItems());
        data.put("displayPagesMenu", this.isDisplayPagesMenu());
        data.put("displayCustomerAgreement", this.isDisplayCustomerAgreement());
        data.put("allowPurchaseItems", this.isAllowPurchaseItems());
        data.put("displaySearchBox", this.displaySearchBox);
        data.put("testMode", this.isTestMode());
        data.put("debugMode", this.isDebugMode());

        if (useDefaultSearchConfig != null) {
            JSONObject obj = new JSONObject();
            for (String key : useDefaultSearchConfig.keySet()) {
                Boolean val = useDefaultSearchConfig.get(key);
                if (val != null) {
                    obj.put(key, val);
                }
            }
            data.put("useDefaultSearchConfig", obj);
        }

        if (defaultSearchConfigPath != null) {
            JSONObject obj = new JSONObject();
            for (String key : defaultSearchConfigPath.keySet()) {
                String val = defaultSearchConfigPath.get(key);
                if (!StringUtils.isBlank(val)) {
                    obj.put(key, val);
                }
            }
            data.put("defaultSearchConfigPath", obj);
        }

        return data.toJSONString();
    }
}
