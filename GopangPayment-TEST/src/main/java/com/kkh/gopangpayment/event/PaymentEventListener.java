package com.kkh.gopangpayment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    // StreamBridge를 주입받는 생성자
    private StreamBridge streamBridge;
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionAfterCommit(PaymentEvent event) {

        streamBridge.send("paymentStatus-topic", MessageBuilder
                .withPayload(null)
                .build()
        );
    }

//    // 주문 취소 이벤트 전 commit 이전에 발생하는 트랜잭션 이벤트를 처리하는 메서드
//    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
//    public void handleTransactionBeforeCommit(OrderCancelEvent cancelEvent) {
//        // 로깅: 주문 취소 이벤트 메시지를 paymentCancelRequest-topic으로 전송
//        log.info("Received message to paymentCancelRequest-topic: " + cancelEvent.getMessage());
//        streamBridge.send("paymentCancelRequest-topic", MessageBuilder
//                .withPayload(cancelEvent.getPaymentRequest())
//                .build()
//        );
//    }
}