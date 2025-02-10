package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.PagamentoLog;
import com.shadow.dashboard.models.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PagamentoLogRepository extends JpaRepository<PagamentoLog, Long> {

    // Busca todos os pagamentos de um determinado empréstimo (histórico)
    List<PagamentoLog> findByHistorico(Historico historico);
}
