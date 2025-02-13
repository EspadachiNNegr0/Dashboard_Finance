package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Parcelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ParcelasRepository extends JpaRepository<Parcelas, Long> {
    
    List<Parcelas> findByHistoricoId(Long historicoId);

    List<Parcelas> findByHistorico(Historico historico);

    @Query("SELECT p FROM Parcelas p WHERE YEAR(p.dataPagamento) = :year AND MONTH(p.dataPagamento) = :month")
    List<Parcelas> findByMonthAndYear(int month, int year);

    @Query("SELECT DISTINCT YEAR(p.dataPagamento) FROM Parcelas p ORDER BY YEAR(p.dataPagamento) DESC")
    List<Integer> findDistinctYears();
}
