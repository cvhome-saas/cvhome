package com.asrevo.cvhome.keycloak.service.impl;

import com.asrevo.cvhome.commons.domain.Groups;
import com.asrevo.cvhome.commons.domain.IdentityId;
import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.UserOrgStoreIdentity;
import com.asrevo.cvhome.commons.utils.OperationExecution;
import com.asrevo.cvhome.keycloak.domain.group.GroupEntity;
import com.asrevo.cvhome.keycloak.domain.user.*;
import com.asrevo.cvhome.keycloak.mappers.UserRepresentationMapper;
import com.asrevo.cvhome.keycloak.service.UserAccountService;
import com.asrevo.cvhome.keycloak.utils.ErrorCodes;
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
import java.util.List;
import java.util.Map;

import static com.asrevo.cvhome.keycloak.utils.Constants.ORG_ATTR_KEY;
import static com.asrevo.cvhome.keycloak.utils.Constants.STORE_ATTR_KEY;

public class KeycloakUserAccountServiceImpl implements UserAccountService {
    private final UserRepresentationMapper userRepresentationMapper;
    private final UsersResource usersResource;

    public KeycloakUserAccountServiceImpl(URI jwkSetUri) {
        Keycloak keycloak = createKeycloak(jwkSetUri);
        this.usersResource = keycloak.realm(jwkSetUri.getPath().split("/")[2]).users();
        this.userRepresentationMapper = new UserRepresentationMapper() {
        };
    }




    @Override
    public ReadableUserList list(ListUsersQuery listUsers) {
        List<UserRepresentation> list = usersResource.searchByAttributes(listUsers.query());
        return userRepresentationMapper.toDto(list, (it) -> usersResource.get(it.getId()).groups());
    }

    private ReadableUser createUser(IdentityId identityId, ManagerStoreId managerStoreId,
                                    PersistableUser persistableUser) {
        try {
            UserRepresentation user = new UserRepresentation();
            user.setEnabled(persistableUser.isActive());
            user.setUsername(persistableUser.getUserName());
            user.setFirstName(persistableUser.getFirstName());
            user.setLastName(persistableUser.getLastName());
            user.setEmail(persistableUser.getEmailAddress());
            Map<String, List<String>> attributes = Map.of(
                    ORG_ATTR_KEY, List.of(identityId.id()),
                    STORE_ATTR_KEY, List.of(managerStoreId.getId().toString())
            );
            user.setAttributes(attributes);
            user.setGroups(persistableUser.getGroups().stream().map(GroupEntity::getName).toList());

            Response response = usersResource.create(user);
            String userId = CreatedResponseUtil.getCreatedId(response);
            UserRepresentation representation = usersResource.get(userId).toRepresentation();
            UserPassword passwordRequestDto = new UserPassword(persistableUser.getPassword(), persistableUser.getRepeatPassword());
            doResetPassword(passwordRequestDto, userId);
            return userRepresentationMapper.toDto(representation, usersResource.get(userId).groups());
        } catch (Exception e) {
            throw new OperationExecution(ErrorCodes.CREATE_USER_FAIL);
        }
    }


    @Override
    public ReadableUser createUser(UserOrgStoreIdentity identity, ManagerStoreId store, PersistableUser create) {
        if (create.getGroups() == null || create.getGroups().isEmpty()) {
            throw new OperationExecution(ErrorCodes.groups_should_not_be_empty);
        }
        if (create.getGroups().stream().anyMatch(it -> it.getName().equals(Groups.CUSTOMER.name()))) {
            throw new OperationExecution(ErrorCodes.create_customer_not_allowed);
        }
        if (create.getGroups().stream().anyMatch(it -> it.getName().equals(Groups.ORG_ADMIN.name()))) {
            throw new OperationExecution(ErrorCodes.create_org_admin_not_allowed);
        }
        if (create.getGroups().stream().anyMatch(it -> it.getName().equals(Groups.SUPER_ADMIN.name()))) {
            throw new OperationExecution(ErrorCodes.create_super_admin_not_allowed);
        }
        if (usernameExist(create.getUserName())) {
            throw new OperationExecution(ErrorCodes.username_already_taken);
        }
        if (emailExist(create.getEmailAddress())) {
            throw new OperationExecution(ErrorCodes.email_already_taken);
        }
        return createUser(identity.org(), store, create);
    }

    @Override
    public void resetPassword(UserOrgStoreIdentity userOrgStoreInfo,
                              ManagerStoreId store,
                              UserPassword passwordRequestDto,
                              String userId) {
        UserResource userResource = usersResource.get(userId);
        UserRepresentation representation = userResource.toRepresentation();
        checkAttrAndValidate(userOrgStoreInfo, store, representation,
                () -> doResetPassword(passwordRequestDto, userId));
    }

    private void doResetPassword(UserPassword passwordRequestDto, String userId) {
        CredentialRepresentation passwordCred = new CredentialRepresentation();
        passwordCred.setTemporary(true);
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

    public Keycloak createKeycloak(URI jwkSetUri) {
        String serverUrl = jwkSetUri.getScheme() + "://" + jwkSetUri.getAuthority();
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("admin-cli")
                .password("admin")
                .username("admin")
                .build();
    }

}

