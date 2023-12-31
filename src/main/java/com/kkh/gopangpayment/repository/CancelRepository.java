package com.kkh.gopangpayment.repository;

import com.kkh.gopangpayment.domain.Cancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CancelRepository extends JpaRepository<Cancel, Long>, JpaSpecificationExecutor<Cancel> {
}
