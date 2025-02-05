package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.PagamentoLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoLogRepository extends JpaRepository<PagamentoLog, Long> {
    List<PagamentoLog> findByHistoricoId(Long historicoId);
}
