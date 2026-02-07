package com.github.gseobi.paygate.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayApproveCommand {
    @NotBlank
    private String paySeq;
    @NotBlank
    private String pgCode;
    private Map<String, String> pgReturnParams;
}
