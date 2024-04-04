package com.asrevo.cvhome.store.core.init;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfig;
import com.asrevo.cvhome.store.core.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.core.services.reference.init.InitializationDatabase;
import com.asrevo.cvhome.store.core.services.system.MerchantConfigurationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class InitializationLoader {

    @Autowired
    protected MerchantStoreService merchantService;
    @Value("${db.init.data:false}")
    private boolean initDefaultData;
    @Autowired
    private MerchantConfigurationService merchantConfigurationService;

    //@Autowired
    //private InitData initData;
    @Autowired
    private InitializationDatabase initializationDatabase;

    @PostConstruct
    public void init() {

        try {

            //Check flag to populate or not the database
            if (!this.initDefaultData) {
                return;
            }

            if (initializationDatabase.isEmpty()) {


                //All default data to be created

                log.info(String.format("%s : Shopizer database is empty, populate it....", "sm-shop"));

                initializationDatabase.populate("sm-shop");

                MerchantStore store = merchantService.getByCode(Constants.DEFAULT_STORE);

                MerchantConfig config = new MerchantConfig();
                config.setAllowPurchaseItems(true);
                config.setDisplayAddToCartOnFeaturedItems(true);

                merchantConfigurationService.saveMerchantConfig(config, store);


            }

        } catch (Exception e) {
            log.error("Error in the init method", e);
        }

    }


}
