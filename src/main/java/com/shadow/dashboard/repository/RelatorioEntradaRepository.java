package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.RelatorioEntrada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioEntradaRepository extends JpaRepository<RelatorioEntrada, Long> {
}
