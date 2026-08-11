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

    private static final String STORE_CREATE = "STORE-POD.MERCHANT.STORE-CREATE";
    private static final String CATALOG_RESERVE = "STORE-POD.CATALOG.RESERVE";
    private static final String MERCHANT_ALL = "STORE-POD.MERCHANT.*";
    private static final String CONTENT_ALL = "STORE-POD.CONTENT.*";
    private static final String CATALOG_ALL = "STORE-POD.CATALOG.*";
    private static final String CHECKOUT_ALL = "STORE-POD.CHECKOUT.*";
    private static final String CUA_ALL = "STORE-POD.CUA.*";
    private static final String PAYMENT_ALL = "STORE-POD.PAYMENT.*";
    private static final String CUSTOMER_ALL = "STORE-POD.CUSTOMER.*";

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
            case STORE_CREATE, CATALOG_RESERVE, MERCHANT_ALL, CONTENT_ALL, CATALOG_ALL, CHECKOUT_ALL,
                 CUA_ALL, PAYMENT_ALL, CUSTOMER_ALL -> hasStorePodPermission(authentication, targetId, action);
            default -> hasStoreCorePermission(authentication, targetId, action);
        };
    }

    private boolean hasStorePodPermission(Authentication authentication, Serializable targetId, String action) {
        return switch (action) {
            case STORE_CREATE -> checker.hasAccessOnStoreCreate(authentication, (String) targetId, this.pod);

            case CATALOG_RESERVE -> checker.isSameStorePod(authentication,
                    new ManagerStoreId(((StoreMerchantId) targetId).storeMerchantId()), this.pod);

            case MERCHANT_ALL, CONTENT_ALL, CATALOG_ALL, CHECKOUT_ALL,
                 CUA_ALL, PAYMENT_ALL -> checker.hasManageAccessOnStore(authentication,
                    new ManagerStoreId(((StoreMerchantId) targetId).storeMerchantId()), this.pod);

            case CUSTOMER_ALL -> checker.isCustomerInSameStore(authentication,
                    new ManagerStoreId(((StoreMerchantId) targetId).storeMerchantId()));

            default -> false;
        };
    }

    private boolean hasStoreCorePermission(Authentication authentication, Serializable targetId, String action) {
        return switch (action) {
            case "STORE-CORE.STORE-FIND-ONE" -> checker.hasAccessOnStoreFindOne(authentication, (ManagerStoreId) targetId);

            case "STORE-CORE.USERS.LIST" -> checker.hasAccessOnStoreUsersList(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.USERS.CREATE" -> checker.hasAccessOnStoreUsersCreate(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.USERS.UPDATE" -> checker.hasAccessOnStoreUsersUpdate(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.USERS.DELETE" -> checker.hasAccessOnStoreUsersDelete(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.USERS.ENABLE" -> checker.hasAccessOnStoreUsersEnable(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.USERS.DISABLE" -> checker.hasAccessOnStoreUsersDisable(authentication, (ManagerStoreId) targetId);
            default -> hasBillingPermission(authentication, targetId, action);
        };
    }

    /**
     * Split out of {@link #hasStoreCorePermission} to keep either switch readable, not because billing is special.
     *
     * <p>
     * Note {@code QUOTA-CHECK} ignores the target: it is asked about an org before any store exists, and only a
     * store-core service principal ever asks it.
     * </p>
     */
    private boolean hasBillingPermission(Authentication authentication, Serializable targetId, String action) {
        return switch (action) {
            case "STORE-CORE.BILLING.READ" -> checker.hasAccessOnBillingRead(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.BILLING.MANAGE" -> checker.hasAccessOnBillingManage(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.BILLING.ENTITLEMENT-READ" ->
                    checker.hasAccessOnBillingEntitlementRead(authentication, (ManagerStoreId) targetId);
            case "STORE-CORE.BILLING.QUOTA-CHECK" -> checker.hasAccessOnBillingQuotaCheck(authentication);
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
