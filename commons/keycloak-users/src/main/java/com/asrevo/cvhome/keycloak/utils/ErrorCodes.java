package com.asrevo.cvhome.keycloak.utils;

import com.asrevo.cvhome.commons.utils.ErrorCode;

public class ErrorCodes {
    public static final ErrorCode KEYCLOAK_USER_ATTR_NOT_CONTAIN_ORG = new ErrorCode("0", "keycloak user attr not contain org");
    public static final ErrorCode KEYCLOAK_USER_ATTR_NOT_CONTAIN_STORE = new ErrorCode("0", "keycloak user attr not contain store");
    public static final ErrorCode NOT_ALLOWED_TO_ACCESS_THIS_ORG_AND_STORE = new ErrorCode("0", "not allowed to access this org and store");
    public static final ErrorCode CREATE_USER_FAIL = new ErrorCode("0", "create user failed");
    public static final ErrorCode CREATE_ORG_USER_FAIL = new ErrorCode("0", "create org user failed");
    public static final ErrorCode CREATE_ORG_ADMIN_NOT_ALLOWED = new ErrorCode("0", "create org admin not allowed");
    public static final ErrorCode CREATE_SUPER_ADMIN_NOT_ALLOWED = new ErrorCode("0", "create super admin not allowed");
    public static final ErrorCode CREATE_CUSTOMER_NOT_ALLOWED = new ErrorCode("0", "create customer not allowed");
    public static final ErrorCode GROUPS_SHOULD_NOT_BE_EMPTY = new ErrorCode("0", "groups should not be empty");
    public static final ErrorCode USERNAME_ALREADY_TAKEN = new ErrorCode("0", "username already taken");
    public static final ErrorCode EMAIL_ALREADY_TAKEN = new ErrorCode("0", "email already taken");
}
