package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Parcelas;
import com.shadow.dashboard.models.StatusParcela;
import org.springframework.data.jpa.repository.JpaRepository;
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

    // Buscar parcelas com status "PENDENTE" e "pagas = 0", ordenadas pela data de pagamento asc.
    Optional<Parcelas> findFirstByPagasAndParcelasGreaterThanOrderByParcelasAsc(int pagas, int parcelas);

    @Query("SELECT COALESCE(SUM(p.valorPago), 0) FROM Parcelas p WHERE p.historico.id = :historicoId")
    double somarValoresPagosPorHistorico(@Param("historicoId") Long historicoId);

    @Query("SELECT COUNT(p) > 0 FROM Parcelas p WHERE p.historico.id = :historicoId AND p.status = 'ABERTA'")
    boolean existeParcelaAberta(@Param("historicoId") Long historicoId);

    List<Parcelas> findByHistoricoId(Long id);

    List<Parcelas> findByHistoricoIdAndStatus(Long id, StatusParcela statusParcela);
}
