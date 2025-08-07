package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Parcelas;
import com.shadow.dashboard.models.RelatorioEntrada;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelatorioEntradaRepository extends JpaRepository<RelatorioEntrada, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM RelatorioEntrada r WHERE r.historico = :historico")
    int deleteByHistorico(@Param("historico") Historico historico);


    boolean existsByHistorico(Historico historico);
}
