package com.github.gseobi.paygate.service.provider.nicepay;

import com.github.gseobi.paygate.api.dto.*;
import com.github.gseobi.paygate.domain.ProviderCode;
import com.github.gseobi.paygate.service.provider.PaymentProviderClient;

import java.util.HashMap;
import java.util.Map;

public class NicepayMockClient implements PaymentProviderClient {

    @Override
    public ProviderCode code() { return ProviderCode.NICEPAY; }

    @Override
    public PayRequestResponse request(PayRequestCommand cmd) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("view", "payment/request/nicepay");
        payload.put("paySeq", cmd.getPaySeq());
        payload.put("amount", cmd.getPayAmount());
        payload.put("payMethodCode", cmd.getPayMethodCode());
        payload.put("mockToken", "NICEPAY-TOKEN-" + cmd.getPaySeq());

        return PayRequestResponse.builder()
                .resultCode("0000")
                .resultMessage("OK")
                .selectedPgCode(code().name())
                .payload(payload)
                .build();
    }

    @Override
    public PayApproveResponse approve(PayApproveCommand cmd) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("approved", true);
        payload.put("pgTid", "NICEPAY-TID-" + cmd.getPaySeq());
        payload.put("raw", cmd.getPgReturnParams());

        return PayApproveResponse.builder()
                .resultCode("0000")
                .resultMessage("APPROVED")
                .pgCode(code().name())
                .payload(payload)
                .build();
    }
}
