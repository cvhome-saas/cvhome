package com.asrevo.cvhome.commons.utils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;

public class Constants {

    public static final String DEFAULT_ORG1_STORE1_STR = "65f023632bc46470c104b76f";

    public static final StoreMerchantId DEFAULT_ORG1_STORE1 = new StoreMerchantId(DEFAULT_ORG1_STORE1_STR);

    public static final StoreMerchantId DEFAULT_ORG1_STORE2 = new StoreMerchantId("65f023632bc46470c104b75f");

    public static final StoreMerchantId DEFAULT_ORG2_STORE1 = new StoreMerchantId("65f020632bc46470c104b76f");

    public static final StoreMerchantId DEFAULT_ORG2_STORE2 = new StoreMerchantId("65f023632bc26470c104b75f");

    private Constants() {
    }

}
