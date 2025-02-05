package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Clientes;
import com.shadow.dashboard.models.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface HistoricoRepository extends JpaRepository<Historico, Long> {

    @Query("SELECT h FROM Historico h WHERE h.cliente.nome LIKE %?1%")
    public List<Historico> findAll(String keyword);

    @Query("SELECT h FROM Historico h WHERE h.created = :created")
    public List<Historico> findByCreated(@Param("created") Date created);

    public List<Historico> findByStatus(String status);

    List<Historico> findByCliente(Clientes cliente);
}
