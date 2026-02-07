package com.github.gseobi.paygate.api;

import com.github.gseobi.paygate.api.dto.*;
import com.github.gseobi.paygate.service.PaymentOrchestrator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentOrchestrator orchestrator;

    /**
     * 결제 요청(포폴용)
     * - 실제는 결제사(Ex. 신한, 카카오페이) WebView 직접 호출 or PG 사에서 제공하는 WebView 가 실행되어야 하나,
     * - Mock 형태로 구성하므로 Sample Response Return
     * */
    @PostMapping("/request")
    public PayRequestResponse request(@RequestBody @Valid PayRequestCommand cmd) {
        return orchestrator.request(cmd);
    }

    /**
     * PG 승인/리턴(포폴용)
     * - 실제는 PG별 return url 이 여러 개로 갈라지지만, 포폴은 단일 엔드포인트 + pgCode로 통합
     */
    @PostMapping("/approve/{pgCode}")
    public PayApproveResponse approve(
            @PathVariable String pgCode,
            @RequestParam Map<String, String> params,
            @RequestParam("paySeq") String paySeq
    ) {
        PayApproveCommand cmd = PayApproveCommand.builder()
                .paySeq(paySeq)
                .pgCode(pgCode)
                .pgReturnParams(params)
                .build();
        return orchestrator.approve(cmd);
    }
}
