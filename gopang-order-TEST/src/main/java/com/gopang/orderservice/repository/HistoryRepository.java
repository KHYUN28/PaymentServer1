package com.gopang.orderservice.repository;

import com.gopang.orderservice.domain.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoryRepository extends JpaRepository<OrderHistory, Long> {
    @Query("SELECT h FROM OrderHistory h WHERE h.user_id=:userId")
    List<OrderHistory> findAllByUserId(@Param("userId") Long userId);
}
