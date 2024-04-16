package com.asrevo.cvhome.store.utils;

import com.asrevo.cvhome.s2s.model.CdnProperties;
import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.content.FileContentType;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CloudFilePathUtils implements AbstractimageFilePath {

    private final CdnProperties cdnProperties;

    public CloudFilePathUtils(CdnProperties cdnProperties) {
        this.cdnProperties = cdnProperties;
    }

    @Override
    public String getBasePath(MerchantStore store) {
        //store has no incidence, basepath drives the url
        return cdnProperties.basePath();
    }


    @Override
    public String getContextPath() {
        return Optional.ofNullable(this.cdnProperties.contextPath()).orElse("");
    }

    /**
     * Builds a static content image file path that can be used by image servlet
     * utility for getting the physical image
     *
     * @param store
     * @param imageName
     * @return
     */
    @Override
    public String buildStaticImageUtils(MerchantStore store, String imageName) {
        StringBuilder imgName = new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH);
        if (!StringUtils.isBlank(imageName)) {
            imgName.append(imageName);
        }
        return imgName.toString();

    }

    /**
     * Builds a static content image file path that can be used by image servlet
     * utility for getting the physical image by specifying the image type
     *
     * @param store
     * @param imageName
     * @return
     */
    @Override
    public String buildStaticImageUtils(MerchantStore store, String type, String imageName) {
        StringBuilder imgName = new StringBuilder().append(getBasePath(store)).append(Constants.FILES_URI).append(Constants.SLASH).append(store.getCode()).append(Constants.SLASH);
        if (type != null && !FileContentType.IMAGE.name().equals(type)) {
            imgName.append(type).append(Constants.SLASH);
        }
        if (!StringUtils.isBlank(imageName)) {
            imgName.append(imageName);
        }
        return imgName.toString();

    }


}
