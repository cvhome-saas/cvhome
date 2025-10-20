package com.asrevo.cvhome.content.utils;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.model.CdnProperties;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.utils.ImageFilePath;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CloudFilePathUtils implements ImageFilePath {

    private final CdnProperties cdnProperties;

    public CloudFilePathUtils(CdnProperties cdnProperties) {
        this.cdnProperties = cdnProperties;
    }

    @Override
    public String getBasePath(StoreMerchantId store) {
        // store has no incidence, basepath drives the url
        return cdnProperties.basePath();
    }

    @Override
    public String getContextPath() {
        return Optional.ofNullable(this.cdnProperties.contextPath()).orElse("");
    }

    /**
     * Builds a static content image file path that can be used by image servlet
     * utility for getting the physical image
     */
    @Override
    public String buildStaticImageUtils(StoreMerchantId store, String imageName) {
        StringBuilder imgName =
                new StringBuilder()
                        .append(getBasePath(store))
                        .append(Constants.FILES_URI)
                        .append(Constants.SLASH)
                        .append(store.getId())
                        .append(Constants.SLASH);
        if (!StringUtils.isBlank(imageName)) {
            imgName.append(imageName);
        }
        return imgName.toString();
    }
}
