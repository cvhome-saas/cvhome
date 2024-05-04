package com.asrevo.cvhome.store.core.init;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.system.MerchantConfig;
import com.asrevo.cvhome.store.core.services.merchant.MerchantStoreService;
import com.asrevo.cvhome.store.core.services.reference.init.InitializationDatabase;
import com.asrevo.cvhome.store.core.services.system.MerchantConfigurationService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_STORE;


@Component
@Slf4j
public class InitializationLoader {

    protected final MerchantStoreService merchantService;
    private final MerchantConfigurationService merchantConfigurationService;
    //@Autowired
    //private InitData initData;
    private final InitializationDatabase initializationDatabase;
    @Value("${db.init.data:false}")
    private boolean initDefaultData;

    public InitializationLoader(MerchantStoreService merchantService, MerchantConfigurationService merchantConfigurationService, InitializationDatabase initializationDatabase) {
        this.merchantService = merchantService;
        this.merchantConfigurationService = merchantConfigurationService;
        this.initializationDatabase = initializationDatabase;
    }

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

                MerchantStore store = merchantService.getByCode(DEFAULT_STORE);

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
