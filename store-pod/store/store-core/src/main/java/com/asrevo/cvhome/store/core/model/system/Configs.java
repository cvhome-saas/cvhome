package com.asrevo.cvhome.store.core.model.system;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class Configs {

    private String facebook;
    private String pinterest;
    private String ga;
    private String instagram;

    private boolean allowOnlinePurchase;
    private boolean displaySearchBox;
    private boolean displayContactUs;
    private boolean displayShipping;

    private boolean displayCustomerSection = false;
    private boolean displayAddToCartOnFeaturedItems = false;
    private boolean displayCustomerAgreement = false;
    private boolean displayPagesMenu = true;
}
