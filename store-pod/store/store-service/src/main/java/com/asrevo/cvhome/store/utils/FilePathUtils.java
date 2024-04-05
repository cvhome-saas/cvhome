package com.asrevo.cvhome.store.utils;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;

import static com.asrevo.cvhome.store.core.constants.Constants.FILES_URI;
import static com.asrevo.cvhome.store.core.constants.Constants.SLASH;

@Component
public class FilePathUtils {


    @Autowired
    @Qualifier("shopizer-properties")
    public Properties properties = new Properties();

    public String buildStaticFilePath(String storeCode, String fileName) {
        String path = FILES_URI + SLASH + storeCode + SLASH;
        if (StringUtils.isNotBlank(fileName)) {
            return path + fileName;
        }
        return path;
    }


}
