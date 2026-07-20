package com.asrevo.cvhome.s2s.config.internal;

import java.io.Serializable;
import java.util.Objects;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import com.asrevo.cvhome.commons.domain.ManagerStoreId;
import com.asrevo.cvhome.commons.domain.Pod;
import com.asrevo.cvhome.commons.domain.StoreMerchantId;
import com.asrevo.cvhome.s2s.model.PodInfoProperties;
import com.asrevo.cvhome.s2s.services.PermissionAccessChecker;

@Configuration
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PermissionAccessChecker checker = new PermissionAccessChecker();

    private final Pod pod;

    public CustomPermissionEvaluator(ApplicationContext context) {
        this.pod = getPod(context);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType,
                                 Object permission) {
        String action = (String) permission;
        return switch (action) {
            // store-pod
            case "STORE-POD.MERCHANT.STORE-CREATE" -> checker.hasAccessOnStoreCreate(authentication, ((String) targetId), this.pod);

            case "STORE-POD.CATALOG.RESERVE" -> checker.isSameStorePod(authentication,
                    new ManagerStoreId(((StoreMerchantId) targetId).storeMerchantId()), this.pod);

            case "STORE-POD.MERCHANT.*", "STORE-POD.CONTENT.*", "STORE-POD.CATALOG.*", "STORE-POD.CHECKOUT.*",
                 "STORE-POD.CUA.*", "STORE-POD.PAYMENT.*" -> checker.hasManageAccessOnStore(authentication,
                    new ManagerStoreId(((StoreMerchantId) targetId).storeMerchantId()), this.pod);

            case "STORE-POD.CUSTOMER.*" -> checker.isCustomerInSameStore(authentication,
                    new ManagerStoreId(((StoreMerchantId) targetId).storeMerchantId()));

            // store-core
            case "STORE-CORE.STORE-FIND-ONE" -> checker.hasAccessOnStoreFindOne(authentication, ((ManagerStoreId) targetId));

            case "STORE-CORE.USERS.LIST" -> checker.hasAccessOnStoreUsersList(authentication, ((ManagerStoreId) targetId));
            case "STORE-CORE.USERS.CREATE" -> checker.hasAccessOnStoreUsersCreate(authentication, ((ManagerStoreId) targetId));
            case "STORE-CORE.USERS.UPDATE" -> checker.hasAccessOnStoreUsersUpdate(authentication, ((ManagerStoreId) targetId));
            case "STORE-CORE.USERS.DELETE" -> checker.hasAccessOnStoreUsersDelete(authentication, ((ManagerStoreId) targetId));
            case "STORE-CORE.USERS.ENABLE" -> checker.hasAccessOnStoreUsersEnable(authentication, ((ManagerStoreId) targetId));
            case "STORE-CORE.USERS.DISABLE" -> checker.hasAccessOnStoreUsersDisable(authentication, ((ManagerStoreId) targetId));
            default -> false;
        };
    }

    private Pod getPod(ApplicationContext context) {
        try {
            ObjectProvider<PodInfoProperties> beanProvider = context.getBeanProvider(PodInfoProperties.class);
            PodInfoProperties properties = beanProvider.getIfAvailable();
            if (Objects.isNull(properties)) {
                return null;
            }
            return properties.pod();
        } catch (Exception _) {
            return null;
        }
    }

}
