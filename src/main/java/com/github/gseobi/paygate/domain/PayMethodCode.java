package com.github.gseobi.paygate.domain;

public enum PayMethodCode {
    SHINHAN,
    WOORI,
    KB,
    NH,
    KAKAOPAY,
    NAVERPAY,
    TOSSPAY,
    APPLEPAY,
    SAMSUNGPAY,
    WECHATPAY,
    ALIPAY;

    public static PayMethodCode from(String s) {
        if (s == null || s.isBlank()) return null;
        return PayMethodCode.valueOf(s.trim().toUpperCase());
    }
}
