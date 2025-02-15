package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Query("SELECT h FROM Historico h WHERE h.cliente.nome LIKE %?1%")
    public List<Historico> findAll(String keyword);


    List<Historico> findByCliente(Clientes cliente);

    @Query("SELECT DISTINCT YEAR(h.created) FROM Historico h ORDER BY YEAR(h.created) DESC")
    List<Integer> findDistinctYears();

    @Query("SELECT h FROM Historico h WHERE YEAR(h.created) = :year")
    List<Historico> findByYear(@Param("year") int year);

    @Query("SELECT h FROM Historico h WHERE MONTH(h.created) = :month AND YEAR(h.created) = :year")
    List<Historico> findByMonthAndYear(@Param("month") int month, @Param("year") int year);

    @Query("SELECT h.socios.name, SUM(h.price) FROM Historico h GROUP BY h.socios.name")
    List<Object[]> sumLoansBySocio();

    @Query("SELECT COUNT(h) FROM Historico h WHERE h.status <> 'PAGO'")
    Long countEmprestimosNaoPagos();

    @Query("SELECT COALESCE(SUM(h.price), 0) FROM Historico h")
    Double sumTotalPrice();

    List<Historico> findByStatus(String status);
}
