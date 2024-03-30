package com.asrevo.cvhome.store.core.services.system;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfig;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfiguration;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfigurationType;
import com.asrevo.cvhome.store.core.exception.ServiceException;
import com.asrevo.cvhome.store.core.repositories.system.MerchantConfigurationRepository;
import com.asrevo.cvhome.store.core.services.generic.SalesManagerEntityServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("merchantConfigurationService")
public class MerchantConfigurationServiceImpl extends
        SalesManagerEntityServiceImpl<Long, MerchantConfiguration> implements
        MerchantConfigurationService {

    private MerchantConfigurationRepository merchantConfigurationRepository;

    @Autowired
    public MerchantConfigurationServiceImpl(
            MerchantConfigurationRepository merchantConfigurationRepository) {
        super(merchantConfigurationRepository);
        this.merchantConfigurationRepository = merchantConfigurationRepository;
    }


    @Override
    public MerchantConfiguration getMerchantConfiguration(String key, MerchantStore store) throws ServiceException {
        return merchantConfigurationRepository.findByMerchantStoreAndKey(store.getId(), key);
    }

    @Override
    public List<MerchantConfiguration> listByStore(MerchantStore store) throws ServiceException {
        return merchantConfigurationRepository.findByMerchantStore(store.getId());
    }

    @Override
    public List<MerchantConfiguration> listByType(MerchantConfigurationType type, MerchantStore store) throws ServiceException {
        return merchantConfigurationRepository.findByMerchantStoreAndType(store.getId(), type);
    }

    @Override
    public void saveOrUpdate(MerchantConfiguration entity) throws ServiceException {


        if (entity.getId() != null && entity.getId() > 0) {
            super.update(entity);
        } else {
            super.create(entity);

        }
    }


    @Override
    public void delete(MerchantConfiguration merchantConfiguration) throws ServiceException {
        MerchantConfiguration config = merchantConfigurationRepository.getOne(merchantConfiguration.getId());
        if (config != null) {
            super.delete(config);
        }
    }

    @Override
    public MerchantConfig getMerchantConfig(MerchantStore store) throws ServiceException {

        MerchantConfiguration configuration = merchantConfigurationRepository.findByMerchantStoreAndKey(store.getId(), MerchantConfigurationType.CONFIG.name());

        MerchantConfig config = null;
        if (configuration != null) {
            String value = configuration.getValue();

            ObjectMapper mapper = new ObjectMapper();
            try {
                config = mapper.readValue(value, MerchantConfig.class);
            } catch (Exception e) {
                throw new ServiceException("Cannot parse json string " + value);
            }
        }
        return config;

    }

    @Override
    public void saveMerchantConfig(MerchantConfig config, MerchantStore store) throws ServiceException {

        MerchantConfiguration configuration = merchantConfigurationRepository.findByMerchantStoreAndKey(store.getId(), MerchantConfigurationType.CONFIG.name());

        if (configuration == null) {
            configuration = new MerchantConfiguration();
            configuration.setMerchantStore(store);
            configuration.setKey(MerchantConfigurationType.CONFIG.name());
            configuration.setMerchantConfigurationType(MerchantConfigurationType.CONFIG);
        }

        String value = config.toJSONString();
        configuration.setValue(value);
        if (configuration.getId() != null && configuration.getId() > 0) {
            super.update(configuration);
        } else {
            super.create(configuration);

        }

    }


}
