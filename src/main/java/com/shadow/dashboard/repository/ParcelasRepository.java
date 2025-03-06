package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Parcelas;
import com.shadow.dashboard.models.StatusParcela;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelasRepository extends JpaRepository<Parcelas, Long> {

    List<Parcelas> findByHistorico(Historico historico);

    @Query("SELECT p FROM Parcelas p WHERE LOWER(p.status) = 'pago'")
    List<Parcelas> findByStatusPago();

    @Query("SELECT p FROM Parcelas p WHERE LOWER(p.status) = 'pendente'")
    List<Parcelas> findByStatusPendente();

    @Query("SELECT p FROM Parcelas p WHERE LOWER(p.status) = 'atrasado'")
    List<Parcelas> findByStatusAtrasado();

    @Query("SELECT p FROM Parcelas p WHERE MONTH(p.dataPagamento) = :mes AND YEAR(p.dataPagamento) = :ano")
    List<Parcelas> findTotalByMonthAndYear(@Param("mes") int mes, @Param("ano") int ano);

    @Query("SELECT DISTINCT YEAR(p.dataPagamento) FROM Parcelas p ORDER BY YEAR(p.dataPagamento) DESC")
    List<Integer> findDistinctYears();

    @Query("SELECT p FROM Parcelas p WHERE YEAR(p.dataPagamento) = :ano")
    List<Parcelas> findParcelasByYear(@Param("ano") int ano);

    @Modifying
    @Transactional
    @Query("DELETE FROM Parcelas p WHERE p.historico = :historico")
    int deleteByHistorico(@Param("historico") Historico historico);

    @Query("SELECT COUNT(p) > 0 FROM Parcelas p WHERE p.historico.id = :historicoId AND p.pagas = 0")
    boolean existeParcelaAberta(@Param("historicoId") Long historicoId);

    List<Parcelas> findByHistoricoIdAndStatus(Long id, StatusParcela statusParcela);

    // ✅ Busca a próxima parcela pendente de um histórico específico, ordenando pela numeração da parcela
    @Query("SELECT p FROM Parcelas p WHERE p.historico = :historico AND p.parcelas > :atual ORDER BY p.parcelas ASC")
    List<Parcelas> findProximaParcela(@Param("historico") Historico historico, @Param("atual") int atual);

    @Query("SELECT p FROM Parcelas p WHERE p.historico = :historico ORDER BY p.parcelas ASC LIMIT 1")
    Parcelas findFirstByHistoricoOrderByParcelasAsc(@Param("historico") Historico historico);


    @Query("SELECT p FROM Parcelas p WHERE p.historico = :historico AND p.parcelas = :codigo")
    Parcelas findByHistoricoAndCodigo(@Param("historico") Historico historico, @Param("codigo") int codigo);

    long countByHistoricoAndStatus(Historico historico, StatusParcela status);

}
