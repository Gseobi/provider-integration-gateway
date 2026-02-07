package com.github.gseobi.paygate.domain;

public enum ProviderCode {
    KCP,
    INICIS,
    NICEPAY;

    public static ProviderCode from(String s) {
        if (s == null || s.isBlank()) return null;
        return ProviderCode.valueOf(s.trim().toUpperCase());
    }
}
