package com.asrevo.cvhome.store.controller.v1.shipping;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;
import com.asrevo.cvhome.store.core.entity.reference.language.Language;
import com.asrevo.cvhome.store.core.model.references.ReadableCountry;
import com.asrevo.cvhome.store.service.facade.shipping.ShippingFacade;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// @TODO ASHRAF
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Shipping - Expedition management resource (Shipping Management Api) - ship to country")
public class ShippingExpeditionApi {


    private final ShippingFacade shippingFacade;

    public ShippingExpeditionApi(ShippingFacade shippingFacade) {
        this.shippingFacade = shippingFacade;
    }


    @GetMapping("/shipping/country")
    public List<ReadableCountry> getCountry(
            @Parameter(hidden = true) MerchantStore merchantStore,
            @Parameter(hidden = true) Language language) {
        return shippingFacade.shipToCountry(merchantStore, language);
    }


}
