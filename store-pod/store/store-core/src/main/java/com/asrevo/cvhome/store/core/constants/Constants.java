package com.asrevo.cvhome.store.core.constants;

import java.util.Currency;
import java.util.Locale;

public class Constants {

    public static final String ALL_REGIONS = "*";
    public final static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public final static String DEFAULT_DATE_FORMAT_YEAR = "yyyy";
    public final static String DEFAULT_LANGUAGE = "en";
    public final static String DEFAULT_COUNTRY = "CA";

    public final static String UNDERSCORE = "_";
    public final static String TRUE = "true";

    public final static String DEFAULT_STORE = "65f023632bc46470c104b76f";
    public static final String DEFAULT_MANUFACTURER = "DEFAULT";
    public final static String DEFAULT_TAX_CLASS = "DEFAULT";
    public final static String DEFAULT_PRICE_DESCRIPTION = "DEFAULT";

    public final static String OT_DISCOUNT_TITLE = "order.total.discount";

    public final static Locale DEFAULT_LOCALE = Locale.US;
    public final static Currency DEFAULT_CURRENCY = Currency.getInstance(Locale.US);

    public final static String SLASH = "/";
    public final static String LANGUAGE = "LANGUAGE";
    public final static String LANG = "lang";

    public final static String MERCHANT_STORE = "MERCHANT_STORE";

    public final static String GROUP_SUPER_ADMIN = "SUPER_ADMIN"; // super admin for all orgs and stores    // new*
    public final static String GROUP_ORG_ADMIN = "ORG_ADMIN"; // store admin
    public final static String GROUP_ADMIN = "ADMIN"; // store admin

    public final static String GROUP_ADMIN_CATALOGUE = "ADMIN_CATALOGUE"; /// moderator
    public final static String GROUP_ADMIN_RETAIL = "ADMIN_RETAIL";  //not have // new*

    public final static String STATIC_URI = "/static";
    public final static String FILES_URI = "/files";
    public final static String PRODUCTS_URI = "/products";
    public final static String SMALL_IMAGE = "SMALL";
    public final static int MAX_REVIEW_RATING_SCORE = 5;

    public final static String OT_ITEM_PRICE_MODULE_CODE = "itemprice";
    public final static String OT_SUBTOTAL_MODULE_CODE = "subtotal";
    public final static String OT_SHIPPING_MODULE_CODE = "shipping";
    public final static String OT_TOTAL_MODULE_CODE = "total";
    public final static String SHIPPING_CONFIGURATION = "SHIPPING_CONFIG";

}
