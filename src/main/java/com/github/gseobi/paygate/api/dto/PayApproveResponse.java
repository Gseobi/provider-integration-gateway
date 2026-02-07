package com.github.gseobi.paygate.api.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayApproveResponse {
    private String resultCode;
    private String resultMessage;
    private String pgCode;
    private Map<String, Object> payload;
}
