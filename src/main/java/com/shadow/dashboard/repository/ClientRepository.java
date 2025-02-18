package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Clientes, Long> {
    Optional<Clientes> findByCpf(String cpf);
}

