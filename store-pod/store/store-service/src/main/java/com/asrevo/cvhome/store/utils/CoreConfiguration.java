package com.asrevo.cvhome.store.utils;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Setter
@Getter
@Component
@Slf4j
public class CoreConfiguration {


    public Properties properties = new Properties();

    public CoreConfiguration() {
    }

    public String getProperty(String propertyKey) {

        return properties.getProperty(propertyKey);


    }

    public String getProperty(String propertyKey, String defaultValue) {

        String prop = defaultValue;
        try {
            prop = properties.getProperty(propertyKey);
        } catch (Exception e) {
            log.warn("Cannot find property " + propertyKey);
        }
        return prop;


    }

}
