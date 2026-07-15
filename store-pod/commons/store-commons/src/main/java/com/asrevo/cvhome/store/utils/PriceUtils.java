package com.asrevo.cvhome.store.utils;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.BigDecimalValidator;
import org.apache.commons.validator.routines.CurrencyValidator;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.MerchantStorePricingBase;
import com.asrevo.cvhome.store.core.model.reference.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;

public class PriceUtils {

    private static final String CANNOT_PARSE = "Cannot parse ";

    private static final char DECIMALCOUNT = '2';

    private static final char DECIMALPOINT = '.';

    private static final char THOUSANDPOINT = ',';

    private PriceUtils() {
    }


    public static String getStringAmount(BigDecimal amount) {

        if (amount == null) {
            return "";
        }

        NumberFormat nf = NumberFormat.getInstance(Constants.DEFAULT_LOCALE);

        nf.setMaximumFractionDigits(Integer.parseInt(Character.toString(DECIMALCOUNT)));
        nf.setMinimumFractionDigits(Integer.parseInt(Character.toString(DECIMALCOUNT)));

        return nf.format(amount);
    }

    public static Locale ofLocal(LanguageCode languageCode, CountryIsoCode countryIsoCode) {
        return Locale.of(languageCode.code(), countryIsoCode.isoCode());
    }

    public static String getStoreFormatedAmountWithCurrency(MerchantStorePricingBase store, BigDecimal amount) {
        if (amount == null) {
            return "";
        }

        Currency currency;
        Locale locale;
        NumberFormat currencyInstance;

        try {
            currency = store.getCurrency().getCurrencyInstance();
            locale = ofLocal(store.getDefaultLanguage(), store.getCountryIsoCode());
        } catch (Exception _) {
            currency = Constants.DEFAULT_CURRENCY.getCurrencyInstance();
            locale = Constants.DEFAULT_LOCALE;
        }

        if (store.isCurrencyFormatNational()) {
            currencyInstance = NumberFormat.getCurrencyInstance(locale); // national
        } else {
            currencyInstance = NumberFormat.getCurrencyInstance(); // international
        }

        currencyInstance.setCurrency(currency);

        return currencyInstance.format(amount.doubleValue());
    }

    public static BigDecimal getAmount(String amount) throws ServiceException {

        StringBuilder newAmount = new StringBuilder();
        for (int i = 0; i < amount.length(); i++) {
            if (amount.charAt(i) != DECIMALPOINT && amount.charAt(i) != THOUSANDPOINT) {
                newAmount.append(amount.charAt(i));
            }
        }

        try {
            Integer.parseInt(newAmount.toString());
        } catch (Exception e) {
            throw new ServiceException(CANNOT_PARSE + amount, e);
        }

        if (!amount.contains(Character.toString(DECIMALPOINT)) && !amount.contains(Character.toString(THOUSANDPOINT))
                && !amount.contains(" ")) {

            if (!matchPositiveInteger(amount)) {
                throw new ServiceException("Not a positive integer " + amount);
            }
            BigDecimalValidator validator = CurrencyValidator.getInstance();
            BigDecimal bdamount = validator.validate(amount, Locale.US);
            if (bdamount == null) {
                throw new ServiceException(CANNOT_PARSE + amount);
            }
            return bdamount;

        } else {

            StringBuilder pat = new StringBuilder();

            if (!StringUtils.isBlank(Character.toString(THOUSANDPOINT))) {
                pat.append("\\d{1,3}(" + THOUSANDPOINT + "?\\d{3})*");
            }

            pat.append("(\\" + DECIMALPOINT + "\\d{1," + DECIMALCOUNT + "})");

            Pattern pattern = Pattern.compile(pat.toString());
            Matcher matcher = pattern.matcher(amount);

            if (matcher.matches()) {

                Locale locale = Constants.DEFAULT_LOCALE;
                BigDecimalValidator validator = CurrencyValidator.getInstance();

                return validator.validate(amount, locale);
            } else {
                throw new ServiceException(CANNOT_PARSE + amount);
            }
        }
    }

    public static boolean matchPositiveInteger(String amount) {
        Pattern pattern = Pattern.compile("^[+]?\\d*$");
        Matcher matcher = pattern.matcher(amount);
        return matcher.matches();
    }

    public static BigDecimal calculatePriceQuantity(BigDecimal price, int quantity) {
        return price.multiply(new BigDecimal(quantity));
    }

}
