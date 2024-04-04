package com.asrevo.cvhome.store.controller.v1.reference;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.references.*;
import com.asrevo.cvhome.store.service.facade.country.CountryFacade;
import com.asrevo.cvhome.store.service.facade.currency.CurrencyFacade;
import com.asrevo.cvhome.store.service.facade.language.LanguageFacade;
import com.asrevo.cvhome.store.service.facade.store.StoreFacade;
import com.asrevo.cvhome.store.service.facade.zone.ZoneFacade;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * Get system Language, Country and Currency objects
 *
 * @author c.samson
 */
@RestController
@RequestMapping("/api/v1")
@Slf4j
public class ReferencesApi {


    @Autowired
    private StoreFacade storeFacade;

    @Autowired
    private LanguageFacade languageFacade;

    @Autowired
    private CountryFacade countryFacade;

    @Autowired
    private ZoneFacade zoneFacade;

    @Autowired
    private CurrencyFacade currencyFacade;

    /**
     * Search languages by language code private/languages returns everything
     *
     * @return
     */
    @GetMapping("/languages")
    public List<Language> getLanguages() {
        return languageFacade.getLanguages();
    }

    /**
     * Returns a country with zones (provinces, states) supports language set in parameter
     * ?lang=en|fr|ru...
     *
     * @param request
     * @return
     */
    @GetMapping("/country")
    public List<ReadableCountry> getCountry(@Parameter(hidden = true) Language language, HttpServletRequest request) {
        MerchantStore merchantStore = storeFacade.getByCode(request);
        return countryFacade.getListCountryZones(language, merchantStore);
    }

    @GetMapping("/zones")
    public List<ReadableZone> getZones(
            @RequestParam("code") String code, @Parameter(hidden = true) Language language, HttpServletRequest request) {
        MerchantStore merchantStore = storeFacade.getByCode(request);
        return zoneFacade.getZones(code, language, merchantStore);
    }

    /**
     * Currency
     *
     * @return
     */
    @GetMapping("/currency")
    public List<Currency> getCurrency() {
        return currencyFacade.getList();
    }

    @GetMapping("/measures")
    public SizeReferences measures() {
        SizeReferences sizeReferences = new SizeReferences();
        sizeReferences.setMeasures(Arrays.asList(MeasureUnit.values()));
        sizeReferences.setWeights(Arrays.asList(WeightUnit.values()));
        return sizeReferences;
    }
}
