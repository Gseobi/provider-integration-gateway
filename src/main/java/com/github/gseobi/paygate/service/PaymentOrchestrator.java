package com.github.gseobi.paygate.service;

import com.github.gseobi.paygate.api.dto.*;
import com.github.gseobi.paygate.config.WeightConfigManager;
import com.github.gseobi.paygate.domain.*;
import com.github.gseobi.paygate.persistence.PaymentSessionRepository;
import com.github.gseobi.paygate.service.provider.PaymentProviderClient;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
public class PaymentOrchestrator {

    private final WeightConfigManager weightConfigManager;
    private final ProviderRouter router;
    private final Map<ProviderCode, PaymentProviderClient> clients;
    private final PaymentSessionRepository sessionRepo;

    public PaymentOrchestrator(
            WeightConfigManager weightConfigManager,
            ProviderRouter router,
            List<PaymentProviderClient> clientList,
            PaymentSessionRepository sessionRepo
    ) {
        this.weightConfigManager = weightConfigManager;
        this.router = router;
        this.sessionRepo = sessionRepo;
        this.clients = clientList.stream().collect(java.util.stream.Collectors.toMap(PaymentProviderClient::code, c -> c));
    }

    public PayRequestResponse request(PayRequestCommand cmd) {
        ProviderCode forced = ProviderCode.from(cmd.getPgCode());
        ProviderCode selected = (forced != null) ? forced : router.choose(weightConfigManager.current());

        PaymentProviderClient client = clients.get(selected);
        if (client == null) {
            return PayRequestResponse.builder()
                    .resultCode("8888")
                    .resultMessage("PG_CLIENT_NOT_FOUND")
                    .selectedPgCode(selected.name())
                    .build();
        }

        PayRequestResponse res = client.request(cmd);

        PaymentSession session = PaymentSession.builder()
                .paySeq(cmd.getPaySeq())
                .pg(selected)
                .amount(cmd.getPayAmount())
                .payMethodCode(cmd.getPayMethodCode())
                .userId(cmd.getUserId())
                .createdAt(Instant.now())
                .lastRequestPayload(res.getPayload())
                .build();
        sessionRepo.save(session);

        log.info("[request] paySeq={} selectedPg={} amount={}", cmd.getPaySeq(), selected, cmd.getPayAmount());
        return res;
    }

    public PayApproveResponse approve(PayApproveCommand cmd) {
        ProviderCode pg = ProviderCode.from(cmd.getPgCode());
        if (pg == null) {
            return PayApproveResponse.builder()
                    .resultCode("8888")
                    .resultMessage("INVALID_PG_CODE")
                    .pgCode(cmd.getPgCode())
                    .build();
        }

        PaymentProviderClient client = clients.get(pg);
        if (client == null) {
            return PayApproveResponse.builder()
                    .resultCode("8888")
                    .resultMessage("PG_CLIENT_NOT_FOUND")
                    .pgCode(pg.name())
                    .build();
        }

        PayApproveResponse res = client.approve(cmd);

        sessionRepo.find(cmd.getPaySeq()).ifPresent(s -> {
            s.setLastApprovePayload(res.getPayload());
            sessionRepo.save(s);
        });

        log.info("[approve] paySeq={} pg={} result={}", cmd.getPaySeq(), pg, res.getResultCode());
        return res;
    }
}
