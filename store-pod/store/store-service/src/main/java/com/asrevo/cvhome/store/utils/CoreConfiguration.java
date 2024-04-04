package com.asrevo.cvhome.store.utils;

import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Setter
@Getter
@Component
public class CoreConfiguration {


    private static final Logger LOGGER = LoggerFactory.getLogger(CoreConfiguration.class);
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
            LOGGER.warn("Cannot find property " + propertyKey);
        }
        return prop;


    }

}
