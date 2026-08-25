package com.asrevo.cvhome.content.service;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.asrevo.cvhome.content.service.binding.PageBinding;
import com.asrevo.cvhome.store.core.entity.content.ContentType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BindingRegistryTest {

    private final BindingRegistry registry = new BindingRegistry(List.of(new PageBinding()));

    @Test
    void aRegisteredTypeIsFoundAndRequired() {
        assertThat(registry.find(ContentType.PAGE)).isPresent();
        assertThat(registry.require(ContentType.PAGE).type()).isEqualTo(ContentType.PAGE);
    }

    @Test
    void anUnregisteredTypeIsEmptyAndRequiringItFailsLoudly() {
        assertThat(registry.find(ContentType.BANNER)).isEmpty();

        assertThatThrownBy(() -> registry.require(ContentType.BANNER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No content binding");
    }

    @Test
    void onlyWorkflowTypesAreListedAsWorkflowTypes() {
        assertThat(registry.workflowTypes()).containsExactly(ContentType.PAGE);
        assertThat(new BindingRegistry(List.of()).workflowTypes()).isEmpty();
    }

}
