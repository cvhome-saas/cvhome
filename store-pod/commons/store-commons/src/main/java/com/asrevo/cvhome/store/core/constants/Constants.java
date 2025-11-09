package com.asrevo.cvhome.store.core.constants;

import com.asrevo.cvhome.store.core.model.reference.CurrencyCode;
import java.util.Currency;
import java.util.Locale;

public class Constants {

	public static final String ALL_REGIONS = "*";

	public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

	public static final String DEFAULT_LANGUAGE = "en";

	public static final String UNDERSCORE = "_";

	public static final String DEFAULT_MANUFACTURER = "DEFAULT";

	public static final String DEFAULT_PRICE_DESCRIPTION = "DEFAULT";

	public static final String OT_DISCOUNT_TITLE = "order.total.discount";

	public static final Locale DEFAULT_LOCALE = Locale.US;

	public static final CurrencyCode DEFAULT_CURRENCY = new CurrencyCode(
			Currency.getInstance(Locale.US).getCurrencyCode());

	public static final String SLASH = "/";

	public static final String LANG = "lang";

	public static final String FILES_URI = "/files";

	public static final String PRODUCTS_URI = "/products";

	public static final String SMALL_IMAGE = "SMALL";

	public static final String OT_ITEM_PRICE_MODULE_CODE = "itemprice";

	public static final String OT_SUBTOTAL_MODULE_CODE = "subtotal";

	public static final String OT_TOTAL_MODULE_CODE = "total";

}
