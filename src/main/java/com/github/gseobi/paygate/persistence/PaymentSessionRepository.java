package com.github.gseobi.paygate.persistence;

import com.github.gseobi.paygate.domain.PaymentSession;

import java.util.Optional;

public interface PaymentSessionRepository {
    void save(PaymentSession s);
    Optional<PaymentSession> find(String paySeq);
}
