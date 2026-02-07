package com.github.gseobi.paygate.config;

import com.github.gseobi.paygate.domain.ProviderCode;
import com.github.gseobi.paygate.domain.ProviderWeight;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class WeightConfigManager {

    private final PayGateProperties props;

    private volatile Map<ProviderCode, Integer> cachedWeights = defaultWeights();
    private final AtomicLong lastLoadedAt = new AtomicLong(0);

    public WeightConfigManager(PayGateProperties props) {
        this.props = props;
    }

    public ProviderWeight current() {
        reloadIfNeeded();
        return new ProviderWeight(cachedWeights);
    }

    private void reloadIfNeeded() {
        long now = System.currentTimeMillis();
        long last = lastLoadedAt.get();
        if (now - last < props.getWeightReloadIntervalMs()) return;

        synchronized (this) {
            long last2 = lastLoadedAt.get();
            if (now - last2 < props.getWeightReloadIntervalMs()) return;

            File f = new File(props.getWeightFilePath());
            if (!f.exists()) {
                log.warn("[WeightConfig] weight file not found: {} -> use default", props.getWeightFilePath());
                lastLoadedAt.set(now);
                return;
            }

            Map<ProviderCode, Integer> newWeights = new EnumMap<>(ProviderCode.class);
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    if (line.isBlank() || line.startsWith("#")) continue;

                    String[] parts = line.split("=");
                    if (parts.length != 2) continue;

                    ProviderCode code = ProviderCode.from(parts[0]);
                    int w = Integer.parseInt(parts[1].trim());
                    if (code != null && w >= 0) newWeights.put(code, w);
                }

                if (!newWeights.isEmpty()) {
                    cachedWeights = newWeights;
                    log.info("[WeightConfig] reloaded weights: {}", cachedWeights);
                } else {
                    log.warn("[WeightConfig] empty file -> keep previous weights");
                }

            } catch (Exception e) {
                log.warn("[WeightConfig] reload failed -> keep previous weights. err={}", e.toString());
            } finally {
                lastLoadedAt.set(now);
            }
        }
    }

    private Map<ProviderCode, Integer> defaultWeights() {
        Map<ProviderCode, Integer> m = new EnumMap<>(ProviderCode.class);
        m.put(ProviderCode.KCP, 34);
        m.put(ProviderCode.INICIS, 33);
        m.put(ProviderCode.NICEPAY, 33);
        return m;
    }
}
