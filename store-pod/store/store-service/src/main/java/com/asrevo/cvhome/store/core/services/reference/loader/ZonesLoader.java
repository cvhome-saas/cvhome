package com.asrevo.cvhome.store.core.services.reference.loader;

import com.asrevo.cvhome.store.core.entity.reference.country.Country;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.reference.zone.Zone;
import com.asrevo.cvhome.store.core.entity.reference.zone.ZoneDescription;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.services.reference.country.CountryService;
import com.asrevo.cvhome.store.core.services.reference.language.LanguageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Drop files in reference/zones with following format
 * <p>
 * <country code>_<language code>.json All lower cases
 *
 * @author carlsamson
 */
@Component
@Slf4j
public class ZonesLoader {

    private static final String PATH = "classpath:/reference/zones/*.json";
    private static final String ALL_REGIONS = "*";
    private final LanguageService languageService;
    private final CountryService countryService;
    private final ResourcePatternResolver resourceResolver;

    public ZonesLoader(LanguageService languageService, CountryService countryService, ResourcePatternResolver resourceResolver) {
        this.languageService = languageService;
        this.countryService = countryService;
        this.resourceResolver = resourceResolver;
    }

    //
    @SuppressWarnings({"rawtypes", "unchecked"})
    public List<Map<String, Zone>> loadIndividualZones() throws Exception {

        List<Map<String, Zone>> loadedZones = new ArrayList<Map<String, Zone>>();
        try {

            List<Resource> files = geZoneFiles(PATH);
            List<Language> languages = languageService.list();

            ObjectMapper mapper = new ObjectMapper();

            List<Country> countries = countryService.list();
            Map<String, Country> countriesMap = new HashMap<String, Country>();
            for (Country country : countries) {
                countriesMap.put(country.getIsoCode(), country);
            }

            Map<String, Zone> zonesMap = new LinkedHashMap<String, Zone>();
            Map<String, List<ZoneDescription>> zonesDescriptionsMap = new LinkedHashMap<String, List<ZoneDescription>>();
            Map<String, String> zonesMark = new LinkedHashMap<String, String>();

            // load files individually
            for (Resource resource : files) {
                InputStream in = resource.getInputStream();
                if (in == null) {
                    continue;
                }
                Map<String, Object> data = mapper.readValue(in, Map.class);

                if (resource.getFilename().contains("_")) {
                    for (Language l : languages) {
                        if (resource.getFilename().contains("_" + l.getCode())) {// lead for this
                            // language
                            List langList = (List) data.get(l.getCode());
                            if (langList != null) {
                                /**
                                 * submethod
                                 */
                                for (Object z : langList) {
                                    Map<String, String> e = (Map<String, String>) z;
                                    mapZone(l, zonesDescriptionsMap, countriesMap, zonesMap, zonesMark, e);
                                }
                            }
                        }
                    }
                } else {
                    List langList = (List) data.get(ALL_REGIONS);
                    if (langList != null) {
                        /**
                         * submethod
                         */
                        for (Language l : languages) {
                            for (Object z : langList) {
                                Map<String, String> e = (Map<String, String>) z;
                                mapZone(l, zonesDescriptionsMap, countriesMap, zonesMap, zonesMark, e);
                            }
                        }
                    }
                }

                for (Map.Entry<String, Zone> entry : zonesMap.entrySet()) {
                    String key = entry.getKey();
                    Zone value = entry.getValue();

                    // get descriptions
                    List<ZoneDescription> descriptions = zonesDescriptionsMap.get(key);
                    if (descriptions != null) {
                        value.setDescriptions(descriptions);
                    }
                }

                loadedZones.add(zonesMap);
            }
            return loadedZones;

        } catch (Exception e) {
            throw new ServiceException(e);
        }

    }


    private InputStream loadFileContent(String fileName) throws Exception {
        return this.getClass().getClassLoader().getResourceAsStream("classpath:/reference/zones/" + fileName);
    }

    public Map<String, Zone> loadZones(String jsonFilePath) throws Exception {

        List<Language> languages = languageService.list();

        List<Country> countries = countryService.list();
        Map<String, Country> countriesMap = new HashMap<String, Country>();
        for (Country country : countries) {

            countriesMap.put(country.getIsoCode(), country);

        }

        ObjectMapper mapper = new ObjectMapper();

        try {

            InputStream in = this.getClass().getClassLoader().getResourceAsStream(jsonFilePath);

            @SuppressWarnings("unchecked")
            Map<String, Object> data = mapper.readValue(in, Map.class);

            Map<String, Zone> zonesMap = new HashMap<String, Zone>();
            Map<String, List<ZoneDescription>> zonesDescriptionsMap = new HashMap<String, List<ZoneDescription>>();
            Map<String, String> zonesMark = new HashMap<String, String>();

            for (Language l : languages) {
                @SuppressWarnings("rawtypes")
                List langList = (List) data.get(l.getCode());
                if (langList != null) {
                    /**
                     * submethod
                     */
                    for (Object z : langList) {
                        @SuppressWarnings("unchecked")
                        Map<String, String> e = (Map<String, String>) z;
                        this.mapZone(l, zonesDescriptionsMap, countriesMap, zonesMap, zonesMark, e);

                    }
                }

            }

            for (Map.Entry<String, Zone> entry : zonesMap.entrySet()) {
                String key = entry.getKey();
                Zone value = entry.getValue();

                // get descriptions
                List<ZoneDescription> descriptions = zonesDescriptionsMap.get(key);
                if (descriptions != null) {
                    value.setDescriptions(descriptions);
                }
            }

            return zonesMap;

        } catch (Exception e) {
            throw new ServiceException(e);
        }

    }

    // internal complex mapping stuff, don't try this at home ...
    private void mapZone(Language l, Map<String, List<ZoneDescription>> zonesDescriptionsMap,
                         Map<String, Country> countriesMap, Map<String, Zone> zonesMap, Map<String, String> zonesMark,
                         Map<String, String> list) {

        String zoneCode = list.get("zoneCode");
        ZoneDescription zoneDescription = new ZoneDescription();
        zoneDescription.setLanguage(l);
        zoneDescription.setName(list.get("zoneName"));
        Zone zone = null;
        List<ZoneDescription> descriptions = null;
        if (!zonesMap.containsKey(zoneCode)) {
            zone = new Zone();
            Country country = countriesMap.get(list.get("countryCode"));
            if (country == null) {
                log.warn("Country is null for " + zoneCode + " and country code " + list.get("countryCode"));
                return;
            }
            zone.setCountry(country);
            zone.setCode(zoneCode);
            zonesMap.put(zoneCode, zone);


        }

        if (zonesMark.containsKey(l.getCode() + "_" + zoneCode)) {
            log.warn("This zone seems to be a duplicate !  " + zoneCode + " and language code " + l.getCode());
            return;
        }

        zonesMark.put(l.getCode() + "_" + zoneCode, l.getCode() + "_" + zoneCode);

        if (zonesDescriptionsMap.containsKey(zoneCode)) {
            descriptions = zonesDescriptionsMap.get(zoneCode);
        } else {
            descriptions = new ArrayList<ZoneDescription>();
            zonesDescriptionsMap.put(zoneCode, descriptions);
        }

        descriptions.add(zoneDescription);

    }

    private List<Resource> geZoneFiles(String path) throws IOException {
        Resource[] resources = resourceResolver.getResources(PATH);

        List<Resource> files = new ArrayList<>();
        Collections.addAll(files, resources);
        return files;

    }


}

