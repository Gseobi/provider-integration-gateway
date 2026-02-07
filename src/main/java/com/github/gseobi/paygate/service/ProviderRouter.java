package com.github.gseobi.paygate.service;

import com.github.gseobi.paygate.domain.ProviderCode;
import com.github.gseobi.paygate.domain.ProviderWeight;

import java.security.SecureRandom;
import java.util.Map;

public class ProviderRouter {

    private final SecureRandom rnd = new SecureRandom();

    public ProviderCode choose(ProviderWeight weight) {
        Map<ProviderCode, Integer> w = weight.weights();
        int total = w.values().stream().mapToInt(Integer::intValue).sum();
        if (total <= 0) {
            // 전부 0이면 fallback
            return ProviderCode.KCP;
        }
        int r = rnd.nextInt(total);
        int acc = 0;
        for (var e : w.entrySet()) {
            acc += e.getValue();
            if (r < acc) return e.getKey();
        }
        return ProviderCode.KCP;
    }
}
