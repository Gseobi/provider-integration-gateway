package com.github.gseobi.paygate;

import com.github.gseobi.paygate.config.PayGateProperties;
import com.github.gseobi.paygate.config.WeightConfigManager;
import com.github.gseobi.paygate.persistence.InMemoryPaymentSessionRepository;
import com.github.gseobi.paygate.persistence.PaymentSessionRepository;
import com.github.gseobi.paygate.service.PaymentOrchestrator;
import com.github.gseobi.paygate.service.ProviderRouter;
import com.github.gseobi.paygate.service.provider.PaymentProviderClient;
import com.github.gseobi.paygate.service.provider.inicis.InicisMockClient;
import com.github.gseobi.paygate.service.provider.kcp.KcpMockClient;
import com.github.gseobi.paygate.service.provider.nicepay.NicepayMockClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
@EnableConfigurationProperties(PayGateProperties.class)
public class PayGateApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayGateApplication.class, args);
    }

    @Bean
    public WeightConfigManager weightConfigManager(PayGateProperties props) {
        return new WeightConfigManager(props);
    }

    @Bean
    public ProviderRouter providerRouter() {
        return new ProviderRouter();
    }

    @Bean
    public PaymentSessionRepository paymentSessionRepository() {
        return new InMemoryPaymentSessionRepository();
    }

    @Bean
    public List<PaymentProviderClient> providerClients() {
        return List.of(
                new KcpMockClient(),
                new InicisMockClient(),
                new NicepayMockClient()
        );
    }

    @Bean
    public PaymentOrchestrator paymentOrchestrator(
            WeightConfigManager weightConfigManager,
            ProviderRouter router,
            List<PaymentProviderClient> clients,
            PaymentSessionRepository repo
    ) {
        return new PaymentOrchestrator(weightConfigManager, router, clients, repo);
    }
}
