package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.RelatorioSaida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioSaidaRepository extends JpaRepository<RelatorioSaida, Long> {

    @Modifying
    @Query("DELETE FROM RelatorioSaida r WHERE r.historico = :historico")
    int deleteByHistorico(@Param("historico") Historico historico);

    boolean existsByHistorico(Historico historico);
}
