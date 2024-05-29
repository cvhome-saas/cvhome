package com.asrevo.cvhome.store.service.facade.user;

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
     */
    boolean authorizedStore(String userName, String merchantStoreCode);


    /**
     * Determines if a user is in a specific group
     *
     */
    void authorizedGroup(String userName, List<String> groupNames);

    /**
     * Check if user is in specific list of roles
     *
     */
    boolean userInRoles(String userName, List<String> groupNames);


    /**
     * Retrieve authenticated user
     *
     */
    String authenticatedUser();
}
