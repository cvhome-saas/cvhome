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
import com.asrevo.cvhome.payment.errors.PaymentConfigurationNotFoundException;
import com.asrevo.cvhome.payment.models.PersistablePaymentConfiguration;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.payment.service.PaymentConfigurationService;
import com.asrevo.cvhome.store.core.entity.common.PaymentStatus;
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

    @PutMapping("/{paymentType}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void updateConfig(StoreMerchantId merchantStore, @PathVariable PaymentType paymentType,
                             @RequestBody PersistablePaymentConfiguration config)
            throws PaymentConfigurationNotFoundException {
        service.updateConfig(merchantStore, paymentType, config);
    }

    @DeleteMapping("/{paymentType}")
    @PreAuthorize("hasPermission(#merchantStore,'StoreMerchantId','STORE-POD.PAYMENT.*')")
    public void deleteConfig(StoreMerchantId merchantStore, @PathVariable PaymentType paymentType)
            throws PaymentConfigurationNotFoundException {
        service.deleteConfig(merchantStore, paymentType);
    }

    /**
     * The two enum lists are the same for every store, so there is nothing tenant-scoped to gate — but they sit under
     * {@code /private/}, and a private handler with no gate is the shape of a missing one (audit A17). Authenticated
     * is the honest audience: any signed-in principal, no store.
     */
    @GetMapping("/supported-payment-types")
    @PreAuthorize("isAuthenticated()")
    public PaymentType[] getSupportedPaymentTypes() {
        return PaymentType.values();
    }

    @GetMapping("/supported-payment-statuses")
    @PreAuthorize("isAuthenticated()")
    public PaymentStatus[] getSupportedPaymentStatuses() {
        return PaymentStatus.values();
    }

}
