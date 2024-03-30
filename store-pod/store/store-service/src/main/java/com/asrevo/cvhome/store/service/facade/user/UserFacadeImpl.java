package com.asrevo.cvhome.store.service.facade.user;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserFacadeImpl implements UserFacade {

    @Override
    public boolean authorizedStore(String userName, String merchantStoreCode) {
        return true;
    }

    @Override
    public void authorizedGroup(String userName, List<String> groupNames) {
        System.out.println("authorizedGroup");
    }

    @Override
    public boolean userInRoles(String userName, List<String> groupNames) {
        return true;
    }

    @Override
    public String authenticatedUser() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
