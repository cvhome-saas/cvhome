package com.asrevo.cvhome.payment.service;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfiguration;
import com.asrevo.cvhome.payment.entity.payment.PaymentConfigurationId;
import com.asrevo.cvhome.payment.errors.PaymentConfigurationNotFoundException;
import com.asrevo.cvhome.payment.mapper.PaymentConfigurationMapper;
import com.asrevo.cvhome.payment.models.PersistablePaymentConfiguration;
import com.asrevo.cvhome.payment.models.ReadablePaymentConfiguration;
import com.asrevo.cvhome.payment.repository.payment.PaymentConfigurationRepository;
import com.asrevo.cvhome.store.core.entity.payments.PaymentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentConfigurationServiceTest {

    private static final StoreMerchantId STORE = new StoreMerchantId("store-1");

    @Mock
    private PaymentConfigurationRepository repository;

    @Mock
    private PaymentConfigurationMapper mapper;

    private PaymentConfigurationService service;

    private static PaymentConfiguration entity(PaymentType type, boolean enabled) {
        PaymentConfiguration entity = new PaymentConfiguration();
        entity.setId(new PaymentConfigurationId(STORE, type));
        entity.setEnabled(enabled);
        return entity;
    }

    @BeforeEach
    void setUp() {
        service = new PaymentConfigurationService(repository, mapper);
    }

    @Test
    void listsEveryConfigurationOfTheStoreThroughTheMapper() {
        PaymentConfiguration cod = entity(PaymentType.COD, true);
        ReadablePaymentConfiguration readable = new ReadablePaymentConfiguration();
        when(repository.findAllByIdStoreMerchantId(STORE)).thenReturn(List.of(cod));
        when(mapper.toDTO(cod)).thenReturn(readable);

        assertThat(service.getConfigs(STORE)).containsExactly(readable);
    }

    @Test
    void singleConfigurationIsOptional() {
        PaymentConfiguration stripe = entity(PaymentType.STRIPE, true);
        ReadablePaymentConfiguration readable = new ReadablePaymentConfiguration();
        when(repository.findByIdStoreMerchantIdAndIdPaymentType(STORE, PaymentType.STRIPE))
                .thenReturn(Optional.of(stripe));
        when(mapper.toDTO(stripe)).thenReturn(readable);
        when(repository.findByIdStoreMerchantIdAndIdPaymentType(STORE, PaymentType.COD)).thenReturn(Optional.empty());

        assertThat(service.getConfig(STORE, PaymentType.STRIPE)).contains(readable);
        assertThat(service.getConfig(STORE, PaymentType.COD)).isEmpty();
    }

    @Test
    void saveForcesTheStoreFromThePathNotTheBody() {
        PersistablePaymentConfiguration dto = PersistablePaymentConfiguration.builder()
                .storeMerchantId(new StoreMerchantId("someone-else")).paymentType(PaymentType.COD).build();
        PaymentConfiguration mapped = entity(PaymentType.COD, true);
        when(mapper.toEntity(dto)).thenReturn(mapped);

        service.saveConfig(STORE, dto);

        assertThat(dto.getStoreMerchantId()).isEqualTo(STORE);
        verify(repository).save(mapped);
    }

    @Test
    void updateAppliesTheMapperToTheExistingRow() throws PaymentConfigurationNotFoundException {
        PaymentConfiguration existing = entity(PaymentType.STRIPE, false);
        PersistablePaymentConfiguration dto = new PersistablePaymentConfiguration();
        when(repository.findById(new PaymentConfigurationId(STORE, PaymentType.STRIPE)))
                .thenReturn(Optional.of(existing));

        service.updateConfig(STORE, PaymentType.STRIPE, dto);

        verify(mapper).updateEntity(existing, dto);
        verify(repository).save(existing);
    }

    @Test
    void updateOfAMissingRowIsATypedNotFound() {
        when(repository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateConfig(STORE, PaymentType.PAYPAL, new PersistablePaymentConfiguration()))
                .isInstanceOf(PaymentConfigurationNotFoundException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void deleteRemovesTheRowOrFailsTyped() throws PaymentConfigurationNotFoundException {
        PaymentConfiguration existing = entity(PaymentType.COD, true);
        when(repository.findById(new PaymentConfigurationId(STORE, PaymentType.COD))).thenReturn(Optional.of(existing));
        when(repository.findById(new PaymentConfigurationId(STORE, PaymentType.PAYPAL))).thenReturn(Optional.empty());

        service.deleteConfig(STORE, PaymentType.COD);

        verify(repository).delete(existing);
        assertThatThrownBy(() -> service.deleteConfig(STORE, PaymentType.PAYPAL))
                .isInstanceOf(PaymentConfigurationNotFoundException.class);
    }

    @Test
    void supportedTypesAreTheEnabledOnesOnly() {
        when(repository.findAllByIdStoreMerchantId(STORE)).thenReturn(List.of(entity(PaymentType.COD, true),
                entity(PaymentType.STRIPE, false), entity(PaymentType.MANUAL_TRANSFER, true)));

        assertThat(service.getSupportedPaymentTypes(STORE)).containsExactly(PaymentType.COD, PaymentType.MANUAL_TRANSFER);
    }

}
