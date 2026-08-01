package com.asrevo.cvhome.payment.service;

import java.util.List;
import java.util.Optional;

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

    private static final String PAYMENT_CONFIGURATION_NOT_FOUND_MESSAGE = "PaymentConfiguration not found with id: ";

    private final PaymentConfigurationRepository repository;
    private final PaymentConfigurationMapper mapper;

    public List<ReadablePaymentConfiguration> getConfigs(StoreMerchantId merchantStore) {
        return repository.findAllByIdStoreMerchantId(merchantStore).stream()
                .map(mapper::toDTO)
                .toList();
    }

    public Optional<ReadablePaymentConfiguration> getConfig(StoreMerchantId merchantStore, PaymentType paymentType) {
        return repository.findByIdStoreMerchantIdAndIdPaymentType(merchantStore, paymentType)
                .map(mapper::toDTO);
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
                .orElseThrow(() -> new ResourceNotFoundException(PAYMENT_CONFIGURATION_NOT_FOUND_MESSAGE + id));

        mapper.updateEntity(entity, dto);
        repository.save(entity);
    }

    @Transactional
    public void deleteConfig(StoreMerchantId merchantStore, PaymentType paymentType) {
        PaymentConfigurationId id = new PaymentConfigurationId(merchantStore, paymentType);
        PaymentConfiguration entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(PAYMENT_CONFIGURATION_NOT_FOUND_MESSAGE + id));

        repository.delete(entity);
    }

    public PaymentType[] getSupportedPaymentTypes(StoreMerchantId storeId) {
        return repository.findAllByIdStoreMerchantId(storeId).stream()
                .filter(PaymentConfiguration::isEnabled)
                .map(entity -> entity.getId().getPaymentType())
                .toArray(PaymentType[]::new);
    }
}
