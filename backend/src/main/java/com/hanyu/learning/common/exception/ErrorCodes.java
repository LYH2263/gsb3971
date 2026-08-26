package com.hanyu.learning.common.exception;

public final class ErrorCodes {

    private ErrorCodes() {
    }

    public static final int VALIDATION_ERROR = 40001;
    public static final int AUTH_INVALID_CREDENTIALS = 40101;
    public static final int AUTH_UNAUTHORIZED = 40102;
    public static final int FORBIDDEN = 40301;
    public static final int RESOURCE_NOT_FOUND = 40401;
    public static final int PHONE_ALREADY_USED = 40901;
    public static final int BED_OCCUPIED = 40902;
    public static final int CUSTOMER_STATUS_INVALID = 40903;
    public static final int BUSINESS_CONFLICT = 40904;
    public static final int INTERNAL_ERROR = 50001;
}
