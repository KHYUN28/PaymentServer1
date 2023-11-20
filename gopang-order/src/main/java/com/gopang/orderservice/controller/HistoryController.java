package com.gopang.orderservice.controller;

import com.gopang.orderservice.dto.History;
import com.gopang.orderservice.service.HistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/history")
public class HistoryController {
    private final HistoryService historyService;

    // 주문 조회시(user_id) -> 주문 내역 get 요청
    @GetMapping("/{user_id}")
    public ResponseEntity<List<History.HistoryResponse>> historyList (@PathVariable Long user_id) {
        List<History.HistoryResponse> allHistory = historyService.getAllHistory(user_id);

        for (History.HistoryResponse history: allHistory) {
            System.out.println(history);
        }

        return new ResponseEntity<>(allHistory, HttpStatus.OK);
    }

    @GetMapping("/{order_id}/receipt")
    public ResponseEntity<History.HistoryResponse> historyReceipt (@PathVariable Long order_id) {
        // 상품명, 수량, 거래액
        return null;
    }

    // 주문 내역 삭제시(history_id) -> 주문 내역 delete 요청

}
