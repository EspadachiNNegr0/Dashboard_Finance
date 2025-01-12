package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Clientes, Long> {
}

