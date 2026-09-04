package com.asrevo.cvhome.sso.web.admin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;

import com.asrevo.cvhome.sso.dto.CreateRoleRequest;
import com.asrevo.cvhome.sso.dto.RoleDto;
import com.asrevo.cvhome.sso.dto.UpdateRoleRequest;
import com.asrevo.cvhome.sso.service.RoleService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The roles screen's endpoints.
 *
 * <p>
 * The permission catalogue is static and served from the service's own enumeration, so the form cannot offer a key
 * the server would refuse — a role saved with an unknown key is rejected, and a console that could offer one would
 * be showing an operator a permission that does not exist.
 * </p>
 *
 * <p>
 * Roles are what every authorisation decision in the realm hangs off, so every endpoint here is the platform
 * operator's; the gate is asserted by reflection rather than one method at a time.
 * </p>
 */
class AdminRoleControllerTest {

    private static final UUID ROLE = UUID.fromString("00000000-0000-0000-0000-0000000000a0");

    private final RoleService roleService = mock(RoleService.class);
    private final AdminRoleController controller = new AdminRoleController(roleService);

    @Test
    void theListAndTheSingleRoleDelegateStraightThrough() throws Exception {
        Pageable pageable = PageRequest.of(0, 20);
        RoleDto dto = mock(RoleDto.class);
        when(roleService.findAll(pageable)).thenReturn(new PageImpl<>(List.of(dto)));
        when(roleService.findOne(ROLE)).thenReturn(dto);

        assertThat(controller.roles(pageable)).containsExactly(dto);
        assertThat(controller.role(ROLE)).isSameAs(dto);
    }

    @Test
    void thePermissionCatalogueIsTheServersOwnSoTheFormCannotOfferAkeyItWouldRefuse() {
        assertThat(controller.permissions()).isEqualTo(RoleService.catalogue()).isNotEmpty();
    }

    @Test
    void creatingUpdatingAndDeletingDelegate() throws Exception {
        CreateRoleRequest create = mock(CreateRoleRequest.class);
        UpdateRoleRequest update = mock(UpdateRoleRequest.class);
        RoleDto created = mock(RoleDto.class);
        RoleDto updated = mock(RoleDto.class);
        when(roleService.create(create)).thenReturn(created);
        when(roleService.update(ROLE, update)).thenReturn(updated);

        assertThat(controller.create(create)).isSameAs(created);
        assertThat(controller.update(ROLE, update)).isSameAs(updated);

        controller.delete(ROLE);
        verify(roleService).delete(ROLE);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void everyEndpointOnThisControllerIsThePlatformOperatorsAlone(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s has no @PreAuthorize", endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains("super_admin").contains("SUPER_ADMIN");
    }

    private static Stream<Method> endpoints() {
        return Stream.of(AdminRoleController.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

}
