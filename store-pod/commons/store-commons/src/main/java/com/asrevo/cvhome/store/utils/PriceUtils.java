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

import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.model.MerchantStorePricingBase;
import com.asrevo.cvhome.store.errors.NonPositivePriceException;
import com.asrevo.cvhome.store.errors.PriceNotParseableException;

public final class PriceUtils {

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

    /**
     * Parses a price as entered by a seller.
     *
     * @throws PriceNotParseableException the text is not a number this parser understands
     * @throws NonPositivePriceException  the text is a number, but not a positive one
     */
    public static BigDecimal getAmount(String amount) throws PriceNotParseableException, NonPositivePriceException {
        String newAmount = stripSeparators(amount);
        validateNumeric(newAmount, amount);

        if (isPlainInteger(amount)) {
            return parsePlainInteger(amount);
        }
        return parseDecimal(amount);
    }

    private static String stripSeparators(String amount) {
        StringBuilder newAmount = new StringBuilder();
        for (int i = 0; i < amount.length(); i++) {
            if (amount.charAt(i) != DECIMALPOINT && amount.charAt(i) != THOUSANDPOINT) {
                newAmount.append(amount.charAt(i));
            }
        }
        return newAmount.toString();
    }

    private static void validateNumeric(String newAmount, String originalAmount) throws PriceNotParseableException {
        try {
            Integer.parseInt(newAmount);
        } catch (NumberFormatException e) {
            throw PriceNotParseableException.of(originalAmount, e);
        }
    }

    private static boolean isPlainInteger(String amount) {
        return !amount.contains(Character.toString(DECIMALPOINT)) && !amount.contains(Character.toString(THOUSANDPOINT))
                && !amount.contains(" ");
    }

    private static BigDecimal parsePlainInteger(String amount)
            throws PriceNotParseableException, NonPositivePriceException {
        if (!matchPositiveInteger(amount)) {
            throw NonPositivePriceException.of(amount);
        }
        BigDecimalValidator validator = CurrencyValidator.getInstance();
        BigDecimal bdamount = validator.validate(amount, Locale.US);
        if (bdamount == null) {
            throw PriceNotParseableException.of(amount);
        }
        return bdamount;
    }

    private static BigDecimal parseDecimal(String amount) throws PriceNotParseableException {
        Pattern pattern = Pattern.compile(buildDecimalPattern());
        Matcher matcher = pattern.matcher(amount);

        if (!matcher.matches()) {
            throw PriceNotParseableException.of(amount);
        }

        Locale locale = Constants.DEFAULT_LOCALE;
        BigDecimalValidator validator = CurrencyValidator.getInstance();
        return validator.validate(amount, locale);
    }

    private static String buildDecimalPattern() {
        StringBuilder pat = new StringBuilder();
        if (!StringUtils.isBlank(Character.toString(THOUSANDPOINT))) {
            pat.append(String.format("\\d{1,3}(%s?\\d{3})*", THOUSANDPOINT));
        }
        pat.append(String.format("(\\%s\\d{1,%s})", DECIMALPOINT, DECIMALCOUNT));
        return pat.toString();
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
