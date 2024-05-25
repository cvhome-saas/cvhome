package com.asrevo.cvhome.keycloak.utils;

import com.asrevo.cvhome.commons.utils.ErrorCode;

public class ErrorCodes {
    public static final ErrorCode CREATE_USER_FAIL = new ErrorCode("0", "create_user_fail");
    public static final ErrorCode create_org_admin_not_allowed = new ErrorCode("0", "create org admin not allowed");
    public static final ErrorCode create_super_admin_not_allowed = new ErrorCode("0", "create super admin not allowed");
    public static final ErrorCode create_customer_not_allowed = new ErrorCode("0", "create customer not allowed");
    public static final ErrorCode groups_should_not_be_empty = new ErrorCode("0", "groups should not be empty");
    public static final ErrorCode username_already_taken = new ErrorCode("0", "username already taken");
    public static final ErrorCode email_already_taken = new ErrorCode("0", "email already taken");
    public static ErrorCode KEYCLOAK_USER_ATTR_NOT_CONTAIN_ORG = new ErrorCode("0", "keycloak user attr not contain org");
    public static ErrorCode KEYCLOAK_USER_ATTR_NOT_CONTAIN_STORE = new ErrorCode("0", "keycloak user attr not contain store");
    public static ErrorCode NOT_ALLOWED_TO_ACCESS_THIS_ORG_AND_STORE = new ErrorCode("0", "not allowed to access this org and store");

}
