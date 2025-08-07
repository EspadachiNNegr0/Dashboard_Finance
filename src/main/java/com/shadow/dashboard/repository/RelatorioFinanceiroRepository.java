package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Parcelas;
import com.shadow.dashboard.models.RelatorioFinanceiro;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelatorioFinanceiroRepository extends JpaRepository<RelatorioFinanceiro, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM RelatorioFinanceiro rf WHERE rf.historico = :historico")
    int deleteByHistorico(@Param("historico") Historico historico);

    @Query("SELECT MAX(r.codigo) FROM RelatorioFinanceiro r")
    Integer findMaxCodigo();

    Optional<RelatorioFinanceiro> findByCodigo(int codigo);
}
