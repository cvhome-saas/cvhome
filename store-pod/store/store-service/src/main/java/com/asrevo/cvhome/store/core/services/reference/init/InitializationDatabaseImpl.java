package com.asrevo.cvhome.store.core.services.reference.init;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.constants.SchemaConstant;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.Manufacturer;
import com.asrevo.cvhome.store.core.entity.catalog.product.manufacturer.ManufacturerDescription;
import com.asrevo.cvhome.store.core.entity.catalog.product.type.ProductType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.country.CountryDescription;
import com.asrevo.cvhome.store.core.entity.reference.currency.Currency;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.reference.zone.ZoneDescription;
import com.asrevo.cvhome.store.core.entity.system.optin.Optin;
import com.asrevo.cvhome.store.core.entity.system.optin.OptinType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.catalog.product.manufacturer.ManufacturerService;
import com.asrevo.cvhome.store.core.services.catalog.product.type.ProductTypeService;
import com.asrevo.cvhome.store.core.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.reference.currency.CurrencyService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.asrevo.cvhome.store.core.services.reference.loader.IntegrationModulesLoader;
import com.asrevo.cvhome.store.core.services.reference.loader.ZonesLoader;
import com.asrevo.cvhome.store.core.services.reference.zone.ZoneService;
import com.asrevo.cvhome.store.core.services.system.optin.OptinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.*;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;

@Service("initializationDatabase")
@Slf4j
public class InitializationDatabaseImpl implements InitializationDatabase {

    protected final MerchantStoreService merchantService;
    protected final ProductTypeService productTypeService;
    private final ZoneService zoneService;
    private final LanguageService languageService;
    private final CountryService countryService;
    private final CurrencyService currencyService;

    /*
        @Autowired
        private TaxClassService taxClassService;
    */
    private final ZonesLoader zonesLoader;

    private final ManufacturerService manufacturerService;

/*
    @Autowired
    private ModuleConfigurationService moduleConfigurationService;
*/

    private final OptinService optinService;


    private String name;

    public InitializationDatabaseImpl(MerchantStoreService merchantService, ProductTypeService productTypeService, ZoneService zoneService, LanguageService languageService, CountryService countryService, CurrencyService currencyService, ZonesLoader zonesLoader, IntegrationModulesLoader modulesLoader, ManufacturerService manufacturerService, OptinService optinService) {
        this.merchantService = merchantService;
        this.productTypeService = productTypeService;
        this.zoneService = zoneService;
        this.languageService = languageService;
        this.countryService = countryService;
        this.currencyService = currencyService;
        this.zonesLoader = zonesLoader;
        this.manufacturerService = manufacturerService;
        this.optinService = optinService;
    }

    public boolean isEmpty() {
        return languageService.count() == 0;
    }

    @Transactional
    public void populate(String contextName) throws ServiceException {
        this.name = contextName;

        createLanguages();
        createCountries();
        createZones();
        createCurrencies();
        createSubReferences();
        createModules();
        createMerchant();


    }


    private void createCurrencies() throws ServiceException {
        log.info(String.format("%s : Populating Currencies ", name));

        for (String code : SchemaConstant.CURRENCY_MAP.keySet()) {

            try {
                java.util.Currency c = java.util.Currency.getInstance(code);

                if (c == null) {
                    log.info(String.format("%s : Populating Currencies : no currency for code : %s", name, code));
                }

                //check if it exist

                Currency currency = new Currency();
                currency.setName(c.getCurrencyCode());
                currency.setCurrency(c);
                currencyService.create(currency);

                //System.out.println(l.getCountry() + "   " + c.getSymbol() + "  " + c.getSymbol(l));
            } catch (IllegalArgumentException e) {
                log.info(String.format("%s : Populating Currencies : no currency for code : %s", name, code));
            }
        }
    }

    private void createCountries() throws ServiceException {
        log.info(String.format("%s : Populating Countries ", name));
        List<Language> languages = languageService.list();
        for (String code : SchemaConstant.COUNTRY_ISO_CODE) {
            Locale locale = SchemaConstant.LOCALES.get(code);
            if (locale != null) {
                Country country = new Country(code);
                countryService.create(country);

                for (Language language : languages) {
                    String name = locale.getDisplayCountry(new Locale(language.getCode()));
                    //byte[] ptext = value.getBytes(Constants.ISO_8859_1);
                    //String name = new String(ptext, Constants.UTF_8);
                    CountryDescription description = new CountryDescription(language, name);
                    countryService.addCountryDescription(country, description);
                }
            }
        }
    }

    private void createZones() throws ServiceException {
        log.info(String.format("%s : Populating Zones ", name));
        try {

            Map<String, Zone> zonesMap = new HashMap<>();
            zonesMap = zonesLoader.loadZones("reference/zoneconfig.json");

            this.addZonesToDb(zonesMap);
/*              
              for (Map.Entry<String, Zone> entry : zonesMap.entrySet()) {
            	    String key = entry.getKey();
            	    Zone value = entry.getValue();
            	    if(value.getDescriptions()==null) {
            	    	LOGGER.warn("This zone " + key + " has no descriptions");
            	    	continue;
            	    }
            	    
            	    List<ZoneDescription> zoneDescriptions = value.getDescriptions();
            	    value.setDescriptons(null);

            	    zoneService.create(value);
            	    
            	    for(ZoneDescription description : zoneDescriptions) {
            	    	description.setZone(value);
            	    	zoneService.addDescription(value, description);
            	    }
              }*/

            //lookup additional zones
            //iterate configured languages
            log.info("Populating additional zones");

            //load reference/zones/* (zone config for additional country)
            //example in.json and in-fr.son
            //will load es zones and use a specific file for french es zones
            List<Map<String, Zone>> loadIndividualZones = zonesLoader.loadIndividualZones();

            loadIndividualZones.forEach(this::addZonesToDb);

        } catch (Exception e) {

            throw new ServiceException(e);
        }

    }


    private void addZonesToDb(Map<String, Zone> zonesMap) throws RuntimeException {

        try {

            for (Map.Entry<String, Zone> entry : zonesMap.entrySet()) {
                String key = entry.getKey();
                Zone value = entry.getValue();

                if (value.getDescriptions() == null) {
                    log.warn("This zone {} has no descriptions", key);
                    continue;
                }

                List<ZoneDescription> zoneDescriptions = value.getDescriptions();
                value.setDescriptions(null);

                zoneService.create(value);

                for (ZoneDescription description : zoneDescriptions) {
                    description.setZone(value);
                    zoneService.addDescription(value, description);
                }
            }

        } catch (Exception e) {
            log.error("An error occured while loading zones", e);

        }

    }

    private void createLanguages() throws ServiceException {
        log.info(String.format("%s : Populating Languages ", name));
        for (String code : SchemaConstant.LANGUAGE_ISO_CODE) {
            Language language = new Language(code);
            languageService.create(language);
        }
    }

    private void createMerchant() throws ServiceException {
        log.info(String.format("%s : Creating merchant ", name));

        Date date = new Date(System.currentTimeMillis());

        Language en = languageService.getByCode("en");
        Country ca = countryService.getByCode("CA");
        Currency currency = currencyService.getByCode("CAD");
        Zone qc = zoneService.getByCode("QC");

        List<Language> supportedLanguages = new ArrayList<>();
        supportedLanguages.add(en);

        //create a merchant
        MerchantStore store = new MerchantStore();
        store.setCountry(ca);
        store.setCurrency(currency);
        store.setDefaultLanguage(en);
        store.setInBusinessSince(date);
        store.setZone(qc);
        store.setStorename("Shopizer");
        store.setOrg("d1952c95-312e-4bb9-9a2d-b703d031276f");
        store.setStorephone("888-888-8888");
        store.setCode(DEFAULT_ORG1_STORE1);
        store.setStorecity("My city");
        store.setStoreaddress("1234 Street address");
        store.setStorepostalcode("H2H-2H2");
        store.setStoreEmailAddress("contact@shopizer.com");
        store.setDomainName("localhost:8080");
        store.setStoreTemplate("december");
        store.setRetailer(true);
        store.setLanguages(supportedLanguages);

        merchantService.create(store);

        //	@TODO ASHRAF
/*
        TaxClass taxclass = new TaxClass(TaxClass.DEFAULT_TAX_CLASS);
        taxclass.setMerchantStore(store);

        taxClassService.create(taxclass);
*/

        //create default manufacturer
        Manufacturer defaultManufacturer = new Manufacturer();
        defaultManufacturer.setCode(Constants.DEFAULT_MANUFACTURER);
        defaultManufacturer.setMerchantStore(store);

        ManufacturerDescription manufacturerDescription = new ManufacturerDescription();
        manufacturerDescription.setLanguage(en);
        manufacturerDescription.setName(Constants.DEFAULT_MANUFACTURER);
        manufacturerDescription.setManufacturer(defaultManufacturer);
        manufacturerDescription.setDescription(Constants.DEFAULT_MANUFACTURER);
        defaultManufacturer.getDescriptions().add(manufacturerDescription);

        manufacturerService.create(defaultManufacturer);

        Optin newsletter = new Optin();
        newsletter.setCode(OptinType.NEWSLETTER.name());
        newsletter.setMerchant(store);
        newsletter.setOptinType(OptinType.NEWSLETTER);
        optinService.create(newsletter);


    }

    private void createModules() throws ServiceException {
        //	@TODO ASHRAF
/*
        try {

            List<IntegrationModule> modules = modulesLoader.loadIntegrationModules("reference/integrationmodules.json");
            for (IntegrationModule entry : modules) {
                moduleConfigurationService.create(entry);
            }


        } catch (Exception e) {
            throw new ServiceException(e);
        }
*/


    }

    private void createSubReferences() throws ServiceException {

        log.info(String.format("%s : Loading catalog sub references ", name));


        ProductType productType = new ProductType();
        productType.setCode(ProductType.GENERAL_TYPE);
        productTypeService.create(productType);


    }


}
