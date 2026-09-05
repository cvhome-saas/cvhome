package com.asrevo.cvhome.sso.web.admin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.access.prepost.PreAuthorize;

import com.asrevo.cvhome.sso.dto.IdentityProviderDto;
import com.asrevo.cvhome.sso.dto.IdentityProviderRequest;
import com.asrevo.cvhome.sso.dto.IdpPresetDto;
import com.asrevo.cvhome.sso.dto.IdpTestResult;
import com.asrevo.cvhome.sso.idp.IdentityProviderService;
import com.asrevo.cvhome.sso.idp.IdpPreset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The identity-provider screen's endpoints.
 *
 * <p>
 * Enable and disable are two paths into one service call, and getting them the wrong way round would leave an
 * operator unable to turn a provider off — so each is pinned to the flag it is meant to send rather than merely to
 * "the service was called".
 * </p>
 *
 * <p>
 * The preset catalogue is served from the enum, so the console cannot offer a provider the server has no defaults
 * for; and every endpoint is the platform operator's, asserted by reflection so a lost annotation cannot make the
 * provider registry writable by any authenticated principal.
 * </p>
 */
class AdminIdentityProviderControllerTest {

    private static final UUID PROVIDER = UUID.fromString("00000000-0000-0000-0000-0000000000c0");
    private static final String GOOGLE = "google";
    private static final String CORP = "corp";

    private final IdentityProviderService providers = mock(IdentityProviderService.class);
    private final AdminIdentityProviderController controller = new AdminIdentityProviderController(providers);

    @Test
    void theReadEndpointsDelegateStraightThrough() throws Exception {
        IdentityProviderDto dto = mock(IdentityProviderDto.class);
        when(providers.list()).thenReturn(List.of(dto));
        when(providers.get(PROVIDER)).thenReturn(dto);

        assertThat(controller.list()).containsExactly(dto);
        assertThat(controller.get(PROVIDER)).isSameAs(dto);
    }

    @Test
    void thePresetCatalogueIsServedFromTheEnumSoTheConsoleCannotOfferAnUnknownProvider() {
        List<IdpPresetDto> presets = controller.presets();

        assertThat(presets).hasSize(IdpPreset.catalogue().size()).isNotEmpty();
    }

    @Test
    void creatingAndUpdatingDelegateWithTheBodyTheConsoleSent() throws Exception {
        IdentityProviderRequest request = mock(IdentityProviderRequest.class);
        IdentityProviderDto created = mock(IdentityProviderDto.class);
        IdentityProviderDto updated = mock(IdentityProviderDto.class);
        when(providers.create(request)).thenReturn(created);
        when(providers.update(PROVIDER, request)).thenReturn(updated);

        assertThat(controller.create(request)).isSameAs(created);
        assertThat(controller.update(PROVIDER, request)).isSameAs(updated);
    }

    @Test
    void enableAndDisableEachSendTheFlagTheyName() throws Exception {
        when(providers.setEnabled(PROVIDER, true)).thenReturn(mock(IdentityProviderDto.class));
        when(providers.setEnabled(PROVIDER, false)).thenReturn(mock(IdentityProviderDto.class));

        controller.enable(PROVIDER);
        controller.disable(PROVIDER);

        // Swapped, an operator could no longer turn a misbehaving provider off.
        verify(providers).setEnabled(PROVIDER, true);
        verify(providers).setEnabled(PROVIDER, false);
    }

    @Test
    void deleteTestAndReorderDelegate() throws Exception {
        IdpTestResult result = mock(IdpTestResult.class);
        IdentityProviderDto dto = mock(IdentityProviderDto.class);
        when(providers.test(PROVIDER)).thenReturn(result);
        when(providers.reorder(List.of(GOOGLE, CORP))).thenReturn(List.of(dto));

        controller.delete(PROVIDER);

        assertThat(controller.test(PROVIDER)).isSameAs(result);
        assertThat(controller.reorder(List.of(GOOGLE, CORP))).containsExactly(dto);
        verify(providers).delete(PROVIDER);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void everyEndpointOnThisControllerIsThePlatformOperatorsAlone(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s has no @PreAuthorize", endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains("super_admin").contains("SUPER_ADMIN");
    }

    private static Stream<Method> endpoints() {
        return Stream.of(AdminIdentityProviderController.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

}
