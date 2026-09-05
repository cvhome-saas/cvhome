package com.asrevo.cvhome.checkout.services.reference;

import java.text.Collator;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.asrevo.cvhome.commons.domain.CountryIsoCode;
import com.asrevo.cvhome.commons.domain.LanguageCode;
import com.asrevo.cvhome.store.model.references.ReadableCountry;
import com.asrevo.cvhome.store.utils.LocaleUtils;

@Service
public class CountryServiceImpl implements CountryService {

    private static final Set<String> ISO_CODES = Set.of(Locale.getISOCountries());

    private final Map<String, List<ReadableCountry>> byLanguage = new ConcurrentHashMap<>();

    @Override
    public List<ReadableCountry> all(LanguageCode language) {
        LanguageCode effective = language == null || !language.isLanguage() ? LanguageCode.defaultLanguage() : language;
        return byLanguage.computeIfAbsent(effective.code(), code -> build(LocaleUtils.getLocale(effective)));
    }

    @Override
    public boolean isKnown(String isoCode) {
        return isoCode != null && ISO_CODES.contains(isoCode.toUpperCase(Locale.ROOT));
    }

    private static List<ReadableCountry> build(Locale locale) {
        Collator collator = Collator.getInstance(locale);
        List<String> codes = Arrays.stream(Locale.getISOCountries())
                .sorted((a, b) -> collator.compare(name(a, locale), name(b, locale)))
                .collect(Collectors.toList());
        return codes.stream().map(code -> {
            ReadableCountry country = new ReadableCountry();
            country.setId((long) codes.indexOf(code) + 1);
            country.setCode(new CountryIsoCode(code));
            country.setSupported(true);
            country.setName(name(code, locale));
            return country;
        }).toList();
    }

    private static String name(String code, Locale locale) {
        return Locale.of("", code).getDisplayCountry(locale);
    }
}
