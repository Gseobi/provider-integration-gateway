package com.github.gseobi.paygate.api.dto;

import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayRequestResponse {
    private String resultCode;
    private String resultMessage;
    private String selectedPgCode;
    private Map<String, Object> payload;
}
