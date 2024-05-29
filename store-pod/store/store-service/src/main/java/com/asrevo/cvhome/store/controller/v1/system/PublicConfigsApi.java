package com.asrevo.cvhome.store.controller.v1.system;

import com.asrevo.cvhome.store.core.constants.Constants;
import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.system.Configs;
import com.asrevo.cvhome.store.service.facade.system.MerchantConfigurationFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.asrevo.cvhome.commons.utils.Constants.DEFAULT_ORG1_STORE1;


@RestController
@RequestMapping("/api/v1")
@Slf4j
public class PublicConfigsApi {


    private final MerchantConfigurationFacade configurationFacade;

    public PublicConfigsApi(MerchantConfigurationFacade configurationFacade) {
        this.configurationFacade = configurationFacade;
    }

    /**
     * Get public set of merchant configuration --- allow online purchase --- social links
     *
     */
    @GetMapping("/config")
    @Operation(
            method = "GET",
            description = "Get public configuration for a given merchant store")
    @Parameters({
            @Parameter(name = "store", schema = @Schema(name = "store", type = "string", defaultValue = DEFAULT_ORG1_STORE1)),
            @Parameter(name = "lang", schema = @Schema(name = "lang", type = "string", defaultValue = Constants.DEFAULT_LANGUAGE))
    })
    public Configs getConfig(@Parameter(hidden = true) MerchantStore merchantStore, @Parameter(hidden = true) Language language) {
        return configurationFacade.getMerchantConfig(merchantStore, language);
    }
}
