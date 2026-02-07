package com.github.gseobi.paygate.domain;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSession {
    private String paySeq;
    private ProviderCode pg;
    private long amount;
    private String payMethodCode;
    private String userId;

    private Instant createdAt;
    private Map<String, Object> lastRequestPayload;
    private Map<String, Object> lastApprovePayload;
}
