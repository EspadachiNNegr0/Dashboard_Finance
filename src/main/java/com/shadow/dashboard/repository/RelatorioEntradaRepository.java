package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.RelatorioEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RelatorioEntradaRepository extends JpaRepository<RelatorioEntrada, Long> {

    @Modifying
    @Query("DELETE FROM RelatorioEntrada r WHERE r.historico = :historico")
    int deleteByHistorico(@Param("historico") Historico historico);

    @Query("SELECT r FROM RelatorioEntrada r WHERE r.historico = :historico")
    List<RelatorioEntrada> findByHistorico(@Param("historico") Historico historico);


    boolean existsByHistorico(Historico historicoSalvo);
}
