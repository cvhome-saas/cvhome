package com.asrevo.cvhome.store.service.facade.user;

import com.asrevo.cvhome.store.core.entity.merchant.MerchantStore;

import java.util.List;

/**
 * Access to all methods for managing users
 *
 * @author carlsamson
 */
public interface UserFacade {


    /**
     * Determines if a user is authorized to perform an action on a specific store
     *
     * @param userName
     * @param merchantStoreCode
     * @return
     * @throws Exception
     */
    boolean authorizedStore(String userName, String merchantStoreCode);


    /**
     * Determines if a user is in a specific group
     *
     * @param userName
     * @param groupNames
     */
    void authorizedGroup(String userName, List<String> groupNames);

    /**
     * Check if user is in specific list of roles
     *
     * @param userName
     * @param groupNames
     * @return
     */
    boolean userInRoles(String userName, List<String> groupNames);


    /**
     * Retrieve authenticated user
     *
     * @return
     */
    String authenticatedUser();
}
