package com.asrevo.cvhome.keycloak.service.impl;

import com.asrevo.cvhome.commons.domain.*;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.keycloak.domain.group.GroupEntity;
import com.asrevo.cvhome.keycloak.domain.user.*;
import com.asrevo.cvhome.keycloak.mappers.UserRepresentationMapper;
import com.asrevo.cvhome.keycloak.service.UserAccountService;
import com.asrevo.cvhome.keycloak.utils.ErrorCodes;
import com.asrevo.cvhome.keycloak.utils.KCUserType;
import jakarta.ws.rs.core.Response;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.asrevo.cvhome.keycloak.utils.Constants.*;

public class KeycloakUserAccountServiceImpl implements UserAccountService {
    private final UserRepresentationMapper userRepresentationMapper;
    private final UsersResource usersResource;

    public KeycloakUserAccountServiceImpl(URI jwkSetUri, KeycloakCredentials credentials) {
        Keycloak keycloak = createKeycloak(jwkSetUri, credentials);
        this.usersResource = keycloak.realm(jwkSetUri.getPath().split("/")[2]).users();
        this.userRepresentationMapper = new UserRepresentationMapper() {
        };
    }


    @Override
    public ReadableUser createOrgUser(PersistableUser persistableUser) {
        try {
            UserRepresentation user = userRepresentationMapper.copyPersistableUser(persistableUser);
            Map<String, List<String>> attributes = Map.of(
                    USER_TYPE_ATTR_KEY, List.of(KCUserType.ORG_USER.name())
            );
            user.setAttributes(attributes);
            user.setGroups(List.of(Groups.ORG_ADMIN.name()));

            return doCreateKCUser(persistableUser, user, false);
        } catch (Exception e) {
            throw new OperationExecution(ErrorCodes.CREATE_ORG_USER_FAIL);
        }

    }


    private ReadableUser doCreateKCUser(PersistableUser persistableUser, UserRepresentation user, boolean temporary) {
        Response response = usersResource.create(user);
        String userId = CreatedResponseUtil.getCreatedId(response);
        UserRepresentation representation = usersResource.get(userId).toRepresentation();
        UserPassword passwordRequestDto = new UserPassword(persistableUser.getPassword(), persistableUser.getRepeatPassword());
        doResetPassword(passwordRequestDto, userId, temporary);
        return userRepresentationMapper.toDto(representation, usersResource.get(userId).groups());
    }

    @Override
    public ReadableUser current(String id) {
        UserResource userResource = usersResource.get(id);
        UserRepresentation userRepresentation = userResource.toRepresentation();
        return userRepresentationMapper.toDto(userRepresentation, usersResource.get(id).groups());
    }

    @Override
    public ReadableUserList list(Principal principal, UserOrgStoreIdentity identity, ManagerStoreId store) {

        List<UserRepresentation> allUsers = new ArrayList<>(usersResource.searchByAttributes(new ListUsersQuery(identity.org(), store).query()));

        if (isSupperAdmin(identity)) allUsers.add(0, usersResource.get(identity.org().id()).toRepresentation());

        List<UserRepresentation> usersExceptMe = allUsers.stream().filter(it -> !it.getId().equals(principal.getName())).toList();

        return userRepresentationMapper.toDto(usersExceptMe, (it) -> usersResource.get(it.getId()).groups());
    }

    private ReadableUser createManagedUser(IdentityId identityId, ManagerStoreId managerStoreId,
                                           PersistableUser persistableUser) {
        try {
            UserRepresentation user = userRepresentationMapper.copyPersistableUser(persistableUser);
            Map<String, List<String>> attributes = Map.of(
                    USER_TYPE_ATTR_KEY, List.of(KCUserType.MANAGED_USER.name()),
                    ORG_ATTR_KEY, List.of(identityId.id()),
                    STORE_ATTR_KEY, List.of(managerStoreId.getId().toString())
            );
            user.setAttributes(attributes);
            user.setGroups(persistableUser.getGroups().stream().map(GroupEntity::getName).toList());

            return doCreateKCUser(persistableUser, user, true);
        } catch (Exception e) {
            throw new OperationExecution(ErrorCodes.CREATE_USER_FAIL);
        }
    }


    @Override
    public ReadableUser createManagedUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser create) {
        if (create.getGroups() == null || create.getGroups().isEmpty()) {
            throw new OperationExecution(ErrorCodes.GROUPS_SHOULD_NOT_BE_EMPTY);
        }
        if (create.getGroups().stream().anyMatch(it -> it.getName().equals(Groups.CUSTOMER.name()))) {
            throw new OperationExecution(ErrorCodes.CREATE_CUSTOMER_NOT_ALLOWED);
        }
        if (create.getGroups().stream().anyMatch(it -> it.getName().equals(Groups.ORG_ADMIN.name()))) {
            throw new OperationExecution(ErrorCodes.CREATE_ORG_ADMIN_NOT_ALLOWED);
        }
        if (create.getGroups().stream().anyMatch(it -> it.getName().equals(Groups.SUPER_ADMIN.name()))) {
            throw new OperationExecution(ErrorCodes.CREATE_SUPER_ADMIN_NOT_ALLOWED);
        }
        if (usernameExist(create.getUserName())) {
            throw new OperationExecution(ErrorCodes.USERNAME_ALREADY_TAKEN);
        }
        if (emailExist(create.getEmailAddress())) {
            throw new OperationExecution(ErrorCodes.EMAIL_ALREADY_TAKEN);
        }
        return createManagedUser(identity.org(), store, create);
    }

    @Override
    public ReadableUser updateManagedUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser user) {
        return null;
    }

    @Override
    public void resetPassword(UserOrgStoreIdentity userOrgStoreInfo,
                              ManagerStoreId store,
                              UserPassword passwordRequestDto,
                              String userId,
                              boolean temporary) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, store, representation,
                () -> doResetPassword(passwordRequestDto, userId, temporary));
    }

    private void doResetPassword(UserPassword passwordRequestDto, String userId, boolean temporary) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(temporary);
        passwordCred.setType(CredentialRepresentation.PASSWORD);
        passwordCred.setValue(passwordRequestDto.getPassword());

        UserResource userResource = usersResource.get(userId);
        userResource.resetPassword(passwordCred);
    }

    public boolean usernameExist(String username) {
        List<UserRepresentation> userRepresentations = this.usersResource.searchByUsername(username, Boolean.TRUE);
        return !userRepresentations.isEmpty();
    }

    public boolean emailExist(String email) {
        List<UserRepresentation> userRepresentations = this.usersResource.searchByEmail(email, Boolean.TRUE);
        return !userRepresentations.isEmpty();
    }

    @Override
    public void deleteUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, store, representation, userResource::remove);
    }

    @Override
    public void enableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, store, representation, () -> {
            representation.setEnabled(Boolean.TRUE);
            userResource.update(representation);
        });
    }

    @Override
    public void disableUser(UserOrgStoreIdentity userOrgStoreInfo, ManagerStoreId store, String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, store, representation, () -> {
            representation.setEnabled(Boolean.FALSE);
            userResource.update(representation);
        });
    }

    @Override
    public ReadableUser findOne(UserOrgStoreIdentity identity, String userId) {
        UserResource userResource = usersResource.get(userId);
        return userRepresentationMapper.toDto(userResource.toRepresentation(), userResource.groups());
    }

    private void checkAttrAndValidate(UserOrgStoreIdentity userOrgStoreInfo,
                                      ManagerStoreId store,
                                      UserRepresentation representation,
                                      Runnable runnable) {
        if (attrMatch(representation, userOrgStoreInfo, store)) {
            runnable.run();
        } else {
            throw new OperationExecution(ErrorCodes.NOT_ALLOWED_TO_ACCESS_THIS_ORG_AND_STORE);
        }
    }

    private boolean attrMatch(UserRepresentation representation,
                              UserOrgStoreIdentity userOrgStoreInfo,
                              ManagerStoreId store) {
        if (isSupperAdmin(userOrgStoreInfo)) return true;
        String orgAttr = userRepresentationMapper.extractKey(representation.getAttributes(), ORG_ATTR_KEY)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.KEYCLOAK_USER_ATTR_NOT_CONTAIN_ORG));
        if (!orgAttr.equals(userOrgStoreInfo.org().id())) {
            return false;
        }
        String storeAttr = userRepresentationMapper.extractKey(representation.getAttributes(), STORE_ATTR_KEY)
                .orElseThrow(() -> new OperationExecution(ErrorCodes.KEYCLOAK_USER_ATTR_NOT_CONTAIN_STORE));
        if (!userOrgStoreInfo.store().equals("*")) {
            if (!storeAttr.equals(userOrgStoreInfo.store())) {
                return false;
            }
            return store.getId().toString().equals(userOrgStoreInfo.store());
        } else {
            return storeAttr.equals(store.getId().toString());
        }
    }

    private static boolean isSupperAdmin(UserOrgStoreIdentity userOrgStoreInfo) {
        return userOrgStoreInfo.roles().contains(Roles.ROLE_SUPER_ADMIN);
    }


    public Keycloak createKeycloak(URI jwkSetUri, KeycloakCredentials credentials) {
        String serverUrl = jwkSetUri.getScheme() + "://" + jwkSetUri.getAuthority();
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("cvhome")
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(credentials.clientId())
                .clientSecret(credentials.clientSecret())
                .build();
    }

}

