package com.asrevo.cvhome.payment.api.v1.payment;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.models.PersistablePaymentConfiguration;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.payment.service.PaymentConfigurationService;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/private/payment-configuration")
@RequiredArgsConstructor
public class PaymentConfigurationController {

    private final PaymentConfigurationService service;

    @GetMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public List<ReadablePaymentConfiguration> getConfigs(StoreMerchantId merchantStore) {
        return service.getConfigs(merchantStore);
    }

    @PostMapping
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void saveConfig(StoreMerchantId merchantStore, @RequestBody PersistablePaymentConfiguration config) {
        service.saveConfig(merchantStore, config);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void updateConfig(StoreMerchantId merchantStore, @PathVariable Long id, @RequestBody PersistablePaymentConfiguration config) {
        service.updateConfig(merchantStore, id, config);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void deleteConfig(StoreMerchantId merchantStore, @PathVariable Long id) {
        service.deleteConfig(merchantStore, id);
    }

    @GetMapping("/supported-payment-types")
    public PaymentType[] getSupportedPaymentTypes() {
        return PaymentType.values();
    }

}
