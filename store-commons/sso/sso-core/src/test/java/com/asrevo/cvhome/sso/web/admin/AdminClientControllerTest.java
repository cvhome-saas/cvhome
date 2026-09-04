package com.asrevo.cvhome.sso.web.admin;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.TestingAuthenticationToken;

import com.asrevo.cvhome.sso.client.ClientType;
import com.asrevo.cvhome.sso.dto.ClientDetails;
import com.asrevo.cvhome.sso.dto.ClientSearch;
import com.asrevo.cvhome.sso.dto.ClientStats;
import com.asrevo.cvhome.sso.dto.CreatedClient;
import com.asrevo.cvhome.sso.dto.RotatedSecret;
import com.asrevo.cvhome.sso.security.PrincipalNames;
import com.asrevo.cvhome.sso.service.AdminClientService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The client registry's endpoints.
 *
 * <p>
 * Two things here are not delegation. The disable call attributes the action to a person, resolving the principal
 * name — which is an account id — to the username an operator will read in the audit log, and tolerates an
 * unauthenticated call rather than throwing on {@code authentication.getName()}. And {@code /options} assembles the
 * console's form from the enums themselves, so a value the server would refuse cannot appear in a dropdown.
 * </p>
 *
 * <p>
 * Every endpoint is the platform operator's, so the gate is asserted by reflection over all of them: a method that
 * lost its annotation would be reachable by any authenticated principal, and the client registry is where secrets
 * are minted.
 * </p>
 */
class AdminClientControllerTest {

    private static final String CLIENT = "console";
    private static final String ACCOUNT_ID = "00000000-0000-0000-0000-000000000001";
    private static final String OPERATOR = "ada";
    private static final String QUERY = "con";
    private static final String NEW_SECRET = "new-secret";
    private static final String SCOPES = "scopes";

    private final AdminClientService clients = mock(AdminClientService.class);
    private final PrincipalNames principals = mock(PrincipalNames.class);
    private final AdminClientController controller = new AdminClientController(clients, principals);

    @Test
    void theListPassesTheConsolesFiltersThroughAsAsearch() {
        Pageable pageable = PageRequest.of(1, 20);
        when(clients.listClients(any(ClientSearch.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        Page<?> page = controller.list(QUERY, Boolean.TRUE, ClientType.CONFIDENTIAL, pageable);

        ArgumentCaptor<ClientSearch> captor = ArgumentCaptor.forClass(ClientSearch.class);
        verify(clients).listClients(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue()).isEqualTo(new ClientSearch(QUERY, Boolean.TRUE, ClientType.CONFIDENTIAL));
        assertThat(page).isEmpty();
    }

    @Test
    void anUnfilteredListPassesNullsRatherThanInventingDefaults() {
        when(clients.listClients(any(ClientSearch.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        controller.list(null, null, null, PageRequest.of(0, 20));

        ArgumentCaptor<ClientSearch> captor = ArgumentCaptor.forClass(ClientSearch.class);
        verify(clients).listClients(captor.capture(), any(Pageable.class));
        assertThat(captor.getValue()).isEqualTo(new ClientSearch(null, null, null));
    }

    @Test
    void theReadEndpointsDelegateStraightThrough() throws Exception {
        ClientStats stats = mock(ClientStats.class);
        ClientDetails details = mock(ClientDetails.class);
        when(clients.stats()).thenReturn(stats);
        when(clients.findById(CLIENT)).thenReturn(details);

        assertThat(controller.stats()).isSameAs(stats);
        assertThat(controller.findOne(CLIENT)).isSameAs(details);
    }

    @Test
    void creatingAndUpdatingDelegateWithTheBodyTheConsoleSent() throws Exception {
        ClientDetails request = mock(ClientDetails.class);
        CreatedClient created = mock(CreatedClient.class);
        ClientDetails updated = mock(ClientDetails.class);
        when(clients.create(request)).thenReturn(created);
        when(clients.update(CLIENT, request)).thenReturn(updated);

        assertThat(controller.create(request)).isSameAs(created);
        assertThat(controller.update(CLIENT, request)).isSameAs(updated);
    }

    @Test
    void theLifecycleEndpointsDelegate() throws Exception {
        ClientDetails enabled = mock(ClientDetails.class);
        RotatedSecret rotated = mock(RotatedSecret.class);
        when(clients.enable(CLIENT)).thenReturn(enabled);
        when(clients.rotateSecret(CLIENT)).thenReturn(rotated);
        when(clients.rotateAll()).thenReturn(List.of(rotated));

        assertThat(controller.enable(CLIENT)).isSameAs(enabled);
        assertThat(controller.rotateSecret(CLIENT)).isSameAs(rotated);
        assertThat(controller.rotateAll()).containsExactly(rotated);

        controller.delete(CLIENT);
        controller.revokePreviousSecret(CLIENT);
        controller.resetSecret(CLIENT, new AdminClientController.ResetSecretRequest(NEW_SECRET));

        verify(clients).delete(CLIENT);
        verify(clients).revokePreviousSecret(CLIENT);
        verify(clients).resetSecret(CLIENT, NEW_SECRET);
    }

    @Test
    void disablingIsAttributedToTheOperatorByTheNameTheyWillBeReadAs() throws Exception {
        when(principals.display(ACCOUNT_ID)).thenReturn(OPERATOR);
        when(clients.disable(CLIENT, OPERATOR)).thenReturn(mock(ClientDetails.class));

        controller.disable(CLIENT, new TestingAuthenticationToken(ACCOUNT_ID, null, List.of()));

        // The principal name is an account id; the audit log has to read as a person.
        verify(clients).disable(CLIENT, OPERATOR);
    }

    @Test
    void anUnauthenticatedDisableIsAttributedToNobodyRatherThanThrowing() throws Exception {
        when(clients.disable(CLIENT, null)).thenReturn(mock(ClientDetails.class));

        controller.disable(CLIENT, null);

        verify(clients).disable(CLIENT, null);
    }

    @Test
    void theOptionsFormIsBuiltFromTheEnumsSoItCannotOfferAvalueTheServerWouldRefuse() {
        when(clients.scopeCatalogue()).thenReturn(Map.of(SCOPES, List.of("openid")));

        Map<String, Object> options = controller.getOptions();

        assertThat(options).containsKey(SCOPES);
        assertThat(options.get("clientAuthenticationMethods")).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.LIST).contains("client_secret_basic");
        assertThat(options.get("authorizationGrantTypes")).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.LIST).contains("client_credentials");
        assertThat(options.get("accessTokenFormat")).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.LIST).containsExactly("self-contained", "reference");
        assertThat(options.get("clientTypes")).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.LIST).contains(ClientType.CONFIDENTIAL.name());
        assertThat(options.get("idTokenSignatureAlgorithm")).asInstanceOf(
                org.assertj.core.api.InstanceOfAssertFactories.LIST).contains("RS256");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("endpoints")
    void everyEndpointOnThisControllerIsThePlatformOperatorsAlone(Method endpoint) {
        PreAuthorize gate = endpoint.getAnnotation(PreAuthorize.class);

        assertThat(gate).as("%s has no @PreAuthorize", endpoint.getName()).isNotNull();
        assertThat(gate.value()).contains("super_admin").contains("SUPER_ADMIN");
    }

    private static Stream<Method> endpoints() {
        return Stream.of(AdminClientController.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .sorted((a, b) -> a.getName().compareTo(b.getName()));
    }

}
