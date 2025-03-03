package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.RelatorioEntrada;
import com.shadow.dashboard.models.RelatorioSaida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelatorioSaidaRepository extends JpaRepository<RelatorioSaida, Long> {
}
