package com.github.gseobi.paygate.domain;

import java.util.EnumMap;
import java.util.Map;

public record ProviderWeight(Map<ProviderCode, Integer> weights) {
    public ProviderWeight(Map<ProviderCode, Integer> weights) {
        this.weights = new EnumMap<>(weights);
    }
}
