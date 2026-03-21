package com.github.gseobi.paygate.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("결제 요청 API가 정상 응답한다")
    void requestPaymentReturnsOk() throws Exception {
        mockMvc.perform(post("/api/payments/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "paySeq": "pay-req-001",
                                  "payAmount": 1000,
                                  "pgCode": "KCP",
                                  "payMethodCode": "CARD",
                                  "userId": "user-001"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("0000"))
                .andExpect(jsonPath("$.selectedPgCode").value("KCP"))
                .andExpect(jsonPath("$.payload.view").value("payment/request/kcp"));
    }

    @Test
    @DisplayName("결제 승인 API가 정상 응답한다")
    void approvePaymentReturnsOk() throws Exception {
        mockMvc.perform(post("/api/payments/approve/KCP")
                        .param("paySeq", "pay-app-001")
                        .param("resCd", "0000")
                        .param("resMsg", "OK"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("0000"))
                .andExpect(jsonPath("$.pgCode").value("KCP"))
                .andExpect(jsonPath("$.payload.approved").value(true));
    }

    @Test
    @DisplayName("잘못된 PG 코드 승인 요청 시 INVALID_PG_CODE를 반환한다")
    void approvePaymentWithInvalidPgCodeReturnsError() throws Exception {
        mockMvc.perform(post("/api/payments/approve/INVALID")
                        .param("paySeq", "pay-app-invalid-001")
                        .param("resCd", "0000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("8888"))
                .andExpect(jsonPath("$.resultMessage").value("INVALID_PG_CODE"))
                .andExpect(jsonPath("$.pgCode").value("INVALID"));
    }
}