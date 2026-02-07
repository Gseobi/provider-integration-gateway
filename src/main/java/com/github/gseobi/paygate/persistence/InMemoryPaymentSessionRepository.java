package com.github.gseobi.paygate.persistence;

import com.github.gseobi.paygate.domain.PaymentSession;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPaymentSessionRepository implements PaymentSessionRepository {

    private final ConcurrentHashMap<String, PaymentSession> store = new ConcurrentHashMap<>();

    @Override
    public void save(PaymentSession s) {
        store.put(s.getPaySeq(), s);
    }

    @Override
    public Optional<PaymentSession> find(String paySeq) {
        return Optional.ofNullable(store.get(paySeq));
    }
}
