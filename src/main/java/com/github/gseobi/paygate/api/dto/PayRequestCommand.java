package com.github.gseobi.paygate.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PayRequestCommand {
    @NotBlank
    private String paySeq;
    @Min(1)
    private long payAmount;
    private String pgCode;
    @NotBlank
    private String payMethodCode;
    private String userId;
}
