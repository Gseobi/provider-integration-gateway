package com.github.gseobi.paygate.service.provider;

import com.github.gseobi.paygate.api.dto.PayApproveCommand;
import com.github.gseobi.paygate.api.dto.PayApproveResponse;
import com.github.gseobi.paygate.api.dto.PayRequestCommand;
import com.github.gseobi.paygate.api.dto.PayRequestResponse;
import com.github.gseobi.paygate.domain.ProviderCode;

public interface PaymentProviderClient {
    ProviderCode code();
    PayRequestResponse request(PayRequestCommand cmd);
    PayApproveResponse approve(PayApproveCommand cmd);
}
