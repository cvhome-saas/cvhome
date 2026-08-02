package com.asrevo.cvhome.controlplane.manager.utils;

import com.asrevo.cvhome.commons.utils.ErrorCode;

public final class ErrorCodes {

    public static final ErrorCode store_not_found = new ErrorCode("0", "store not found");

    public static final ErrorCode store_pod_not_match_any = new ErrorCode("1", "store pod not match any");

    private ErrorCodes() {
    }

}
