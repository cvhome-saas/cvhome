package com.asrevo.cvhome.store.core.constants;

import java.util.Currency;
import java.util.Locale;

public class Constants {

    public static final String PAYMENT_MODULES = "PAYMENT";

    public static final String ALL_REGIONS = "*";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATE_FORMAT_YEAR = "yyyy";
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_COUNTRY = "CA";

    public static final String UNDERSCORE = "_";
    public static final String TRUE = "true";

    public static final String DEFAULT_MANUFACTURER = "DEFAULT";
    public static final String DEFAULT_TAX_CLASS = "DEFAULT";
    public static final String DEFAULT_PRICE_DESCRIPTION = "DEFAULT";

    public static final String OT_DISCOUNT_TITLE = "order.total.discount";

    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final Currency DEFAULT_CURRENCY = Currency.getInstance(Locale.US);

    public static final String SLASH = "/";
    public static final String LANGUAGE = "LANGUAGE";
    public static final String LANG = "lang";

    public static final String MERCHANT_STORE = "MERCHANT_STORE";

    public static final String GROUP_SUPER_ADMIN =
            "SUPER_ADMIN"; // super admin for all orgs and stores    // new*
    public static final String GROUP_ORG_ADMIN = "ORG_ADMIN"; // store admin
    public static final String GROUP_ADMIN = "ADMIN"; // store admin

    public static final String GROUP_ADMIN_CATALOGUE = "ADMIN_CATALOGUE"; // / moderator
    public static final String GROUP_ADMIN_RETAIL = "ADMIN_RETAIL"; // not have // new*

    public static final String STATIC_URI = "/static";
    public static final String FILES_URI = "/files";
    public static final String PRODUCTS_URI = "/products";
    public static final String SMALL_IMAGE = "SMALL";
    public static final int MAX_REVIEW_RATING_SCORE = 5;

    public static final int MAX_DOWNLOAD_DAYS = 30;

    public static final String OT_ITEM_PRICE_MODULE_CODE = "itemprice";
    public static final String OT_SUBTOTAL_MODULE_CODE = "subtotal";
    public static final String OT_SHIPPING_MODULE_CODE = "shipping";
    public static final String OT_TOTAL_MODULE_CODE = "total";
    public static final String SHIPPING_CONFIGURATION = "SHIPPING_CONFIG";

    public static final String KEY_FACEBOOK_PAGE_URL = "facebook_page_url";
    public static final String KEY_PINTEREST_PAGE_URL = "pinterest";
    public static final String KEY_GOOGLE_ANALYTICS_URL = "google_analytics_url";
    public static final String KEY_INSTAGRAM_URL = "instagram";

    public static final String OT_HANDLING_MODULE_CODE = "handling";
    public static final String OT_TAX_MODULE_CODE = "tax";
    public static final String OT_REFUND_MODULE_CODE = "refund";
}
