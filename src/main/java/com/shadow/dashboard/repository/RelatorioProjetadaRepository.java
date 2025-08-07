package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Parcelas;
import com.shadow.dashboard.models.RelatorioProjetada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelatorioProjetadaRepository extends JpaRepository<RelatorioProjetada, Long> {

    boolean existsByHistoricoAndParcela(Historico historicoSalvo, Parcelas parcela);

    Optional<RelatorioProjetada> findByParcela(Parcelas parcela);

    @Modifying
    @Query("DELETE FROM RelatorioProjetada rp WHERE rp.historico = :historico")
    int deleteByHistorico(@Param("historico") Historico historico);

    Optional<RelatorioProjetada> findByHistorico(Historico historicoSalvo);

    List<RelatorioProjetada> findByHistoricoAndParcela(Historico historicoSalvo, Parcelas parcela);
}
