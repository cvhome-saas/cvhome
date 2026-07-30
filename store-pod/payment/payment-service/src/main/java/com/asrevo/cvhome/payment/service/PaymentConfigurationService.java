package com.asrevo.cvhome.payment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfigurationId;
import com.asrevo.cvhome.payment.mapper.PaymentConfigurationMapper;
import com.asrevo.cvhome.payment.models.PersistablePaymentConfiguration;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.payment.repository.payment.PaymentConfigurationRepository;
import com.asrevo.cvhome.store.controller.exception.ResourceNotFoundException;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentConfigurationService {

    private final PaymentConfigurationRepository repository;
    private final PaymentConfigurationMapper mapper;

    public List<ReadablePaymentConfiguration> getConfigs(StoreMerchantId merchantStore) {
        return repository.findAllByIdStoreMerchantId(merchantStore).stream()
                .map(mapper::toDTO)
                .toList();
    }

    @Transactional
    public void saveConfig(StoreMerchantId merchantStore, PersistablePaymentConfiguration dto) {
        dto.setStoreMerchantId(merchantStore);
        repository.save(mapper.toEntity(dto));
    }

    @Transactional
    public void updateConfig(StoreMerchantId merchantStore, PaymentType paymentType, PersistablePaymentConfiguration dto) {
        PaymentConfigurationId id = new PaymentConfigurationId(merchantStore, paymentType);
        PaymentConfiguration entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentConfiguration not found with id: " + id));

        mapper.updateEntity(entity, dto);
        repository.save(entity);
    }

    @Transactional
    public void deleteConfig(StoreMerchantId merchantStore, PaymentType paymentType) {
        PaymentConfigurationId id = new PaymentConfigurationId(merchantStore, paymentType);
        PaymentConfiguration entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PaymentConfiguration not found with id: " + id));

        repository.delete(entity);
    }

    public PaymentType[] getSupportedPaymentTypes(StoreMerchantId storeId) {
        return repository.findAllByIdStoreMerchantId(storeId).stream()
                .filter(PaymentConfiguration::isEnabled)
                .map(entity -> entity.getId().getPaymentType())
                .toArray(PaymentType[]::new);
    }
}
