package com.github.gseobi.paygate.service;

import com.github.gseobi.paygate.api.dto.PayApproveCommand;
import com.github.gseobi.paygate.api.dto.PayApproveResponse;
import com.github.gseobi.paygate.api.dto.PayRequestCommand;
import com.github.gseobi.paygate.api.dto.PayRequestResponse;
import com.github.gseobi.paygate.config.PayGateProperties;
import com.github.gseobi.paygate.config.WeightConfigManager;
import com.github.gseobi.paygate.domain.ProviderCode;
import com.github.gseobi.paygate.persistence.InMemoryPaymentSessionRepository;
import com.github.gseobi.paygate.service.provider.PaymentProviderClient;
import com.github.gseobi.paygate.service.provider.inicis.InicisMockClient;
import com.github.gseobi.paygate.service.provider.kcp.KcpMockClient;
import com.github.gseobi.paygate.service.provider.nicepay.NicepayMockClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentOrchestratorTest {

    private PaymentOrchestrator newOrchestrator(List<PaymentProviderClient> clients) {
        PayGateProperties props = new PayGateProperties();
        WeightConfigManager weightConfigManager = new WeightConfigManager(props);
        ProviderRouter router = new ProviderRouter();
        InMemoryPaymentSessionRepository repo = new InMemoryPaymentSessionRepository();
        return new PaymentOrchestrator(weightConfigManager, router, clients, repo);
    }

    @Test
    @DisplayName("KCP 강제 요청 시 KCP 응답이 반환된다")
    void requestWithForcedKcpReturnsKcpResponse() {
        PaymentOrchestrator orchestrator = newOrchestrator(
                List.of(new KcpMockClient(), new InicisMockClient(), new NicepayMockClient())
        );

        PayRequestCommand cmd = PayRequestCommand.builder()
                .paySeq("pay-kcp-001")
                .payAmount(1000L)
                .pgCode("KCP")
                .payMethodCode("CARD")
                .userId("user-001")
                .build();

        PayRequestResponse response = orchestrator.request(cmd);

        assertThat(response.getResultCode()).isEqualTo("0000");
        assertThat(response.getSelectedPgCode()).isEqualTo("KCP");
        assertThat(response.getPayload()).containsEntry("view", "payment/request/kcp");
    }

    @Test
    @DisplayName("INICIS 강제 요청 시 INICIS 응답이 반환된다")
    void requestWithForcedInicisReturnsInicisResponse() {
        PaymentOrchestrator orchestrator = newOrchestrator(
                List.of(new KcpMockClient(), new InicisMockClient(), new NicepayMockClient())
        );

        PayRequestCommand cmd = PayRequestCommand.builder()
                .paySeq("pay-inicis-001")
                .payAmount(2000L)
                .pgCode("INICIS")
                .payMethodCode("CARD")
                .userId("user-002")
                .build();

        PayRequestResponse response = orchestrator.request(cmd);

        assertThat(response.getResultCode()).isEqualTo("0000");
        assertThat(response.getSelectedPgCode()).isEqualTo("INICIS");
        assertThat(response.getPayload()).containsEntry("view", "payment/request/inicis");
    }

    @Test
    @DisplayName("NICEPAY 강제 요청 시 NICEPAY 응답이 반환된다")
    void requestWithForcedNicepayReturnsNicepayResponse() {
        PaymentOrchestrator orchestrator = newOrchestrator(
                List.of(new KcpMockClient(), new InicisMockClient(), new NicepayMockClient())
        );

        PayRequestCommand cmd = PayRequestCommand.builder()
                .paySeq("pay-nicepay-001")
                .payAmount(3000L)
                .pgCode("NICEPAY")
                .payMethodCode("CARD")
                .userId("user-003")
                .build();

        PayRequestResponse response = orchestrator.request(cmd);

        assertThat(response.getResultCode()).isEqualTo("0000");
        assertThat(response.getSelectedPgCode()).isEqualTo("NICEPAY");
        assertThat(response.getPayload()).containsEntry("view", "payment/request/nicepay");
    }

    @Test
    @DisplayName("지원하지 않는 approve pgCode 요청 시 INVALID_PG_CODE를 반환한다")
    void approveWithInvalidPgCodeReturnsInvalidCode() {
        PaymentOrchestrator orchestrator = newOrchestrator(
                List.of(new KcpMockClient(), new InicisMockClient(), new NicepayMockClient())
        );

        PayApproveCommand cmd = PayApproveCommand.builder()
                .paySeq("pay-approve-invalid-001")
                .pgCode("INVALID")
                .pgReturnParams(Map.of("resCd", "0000"))
                .build();

        PayApproveResponse response = orchestrator.approve(cmd);

        assertThat(response.getResultCode()).isEqualTo("8888");
        assertThat(response.getResultMessage()).isEqualTo("INVALID_PG_CODE");
        assertThat(response.getPgCode()).isEqualTo("INVALID");
    }

    @Test
    @DisplayName("선택된 PG에 대한 client가 없으면 PG_CLIENT_NOT_FOUND를 반환한다")
    void requestWithoutClientReturnsClientNotFound() {
        PaymentOrchestrator orchestrator = newOrchestrator(
                List.of(new KcpMockClient(), new InicisMockClient()) // NICEPAY 미등록
        );

        PayRequestCommand cmd = PayRequestCommand.builder()
                .paySeq("pay-no-client-001")
                .payAmount(4000L)
                .pgCode("NICEPAY")
                .payMethodCode("CARD")
                .userId("user-004")
                .build();

        PayRequestResponse response = orchestrator.request(cmd);

        assertThat(response.getResultCode()).isEqualTo("8888");
        assertThat(response.getResultMessage()).isEqualTo("PG_CLIENT_NOT_FOUND");
        assertThat(response.getSelectedPgCode()).isEqualTo(ProviderCode.NICEPAY.name());
    }
}