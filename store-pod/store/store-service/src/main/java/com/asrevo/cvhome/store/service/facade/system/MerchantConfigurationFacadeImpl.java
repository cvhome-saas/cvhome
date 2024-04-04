package com.asrevo.cvhome.store.service.facade.system;

import com.asrevo.cvhome.store.controller.exception.ServiceRuntimeException;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfig;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfiguration;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.model.system.Configs;
import com.asrevo.cvhome.store.core.services.system.MerchantConfigurationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.asrevo.cvhome.store.core.constants.Constants.*;


@Service
@Slf4j
public class MerchantConfigurationFacadeImpl implements MerchantConfigurationFacade {


    @Autowired
    private MerchantConfigurationService merchantConfigurationService;

    @Value("${config.displayShipping:false}")
    private String displayShipping;

    @Override
    public Configs getMerchantConfig(MerchantStore merchantStore, Language language) {

        MerchantConfig configs = getMerchantConfig(merchantStore);

        Configs readableConfig = new Configs();
        readableConfig.setAllowOnlinePurchase(configs.isAllowPurchaseItems());
        readableConfig.setDisplaySearchBox(configs.isDisplaySearchBox());
        readableConfig.setDisplayContactUs(configs.isDisplayContactUs());

        readableConfig.setDisplayCustomerSection(configs.isDisplayCustomerSection());
        readableConfig.setDisplayAddToCartOnFeaturedItems(configs.isDisplayAddToCartOnFeaturedItems());
        readableConfig.setDisplayCustomerAgreement(configs.isDisplayCustomerAgreement());
        readableConfig.setDisplayPagesMenu(configs.isDisplayPagesMenu());

        Optional<String> facebookConfigValue = getConfigValue(KEY_FACEBOOK_PAGE_URL, merchantStore);
        facebookConfigValue.ifPresent(readableConfig::setFacebook);

        Optional<String> googleConfigValue = getConfigValue(KEY_GOOGLE_ANALYTICS_URL, merchantStore);
        googleConfigValue.ifPresent(readableConfig::setGa);

        Optional<String> instagramConfigValue = getConfigValue(KEY_INSTAGRAM_URL, merchantStore);
        instagramConfigValue.ifPresent(readableConfig::setInstagram);


        Optional<String> pinterestConfigValue = getConfigValue(KEY_PINTEREST_PAGE_URL, merchantStore);
        pinterestConfigValue.ifPresent(readableConfig::setPinterest);

        readableConfig.setDisplayShipping(false);
        try {
            if (!StringUtils.isBlank(displayShipping)) {
                readableConfig.setDisplayShipping(Boolean.valueOf(displayShipping));
            }
        } catch (Exception e) {
            log.error("Cannot parse value of " + displayShipping);
        }

        return readableConfig;
    }

    private MerchantConfig getMerchantConfig(MerchantStore merchantStore) {
        try {
            return merchantConfigurationService.getMerchantConfig(merchantStore);
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }
    }

    private Optional<String> getConfigValue(String keyContant, MerchantStore merchantStore) {
        return getMerchantConfiguration(keyContant, merchantStore)
                .map(MerchantConfiguration::getValue);
    }

    private Optional<MerchantConfiguration> getMerchantConfiguration(String key, MerchantStore merchantStore) {
        try {
            return Optional.ofNullable(merchantConfigurationService.getMerchantConfiguration(key, merchantStore));
        } catch (ServiceException e) {
            throw new ServiceRuntimeException(e);
        }

    }
}
