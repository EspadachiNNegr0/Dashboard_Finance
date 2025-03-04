package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.RelatorioEntrada;
import com.shadow.dashboard.models.RelatorioFinanceiro;
import com.shadow.dashboard.models.RelatorioSaida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RelatorioFinanceiroRepository extends JpaRepository<RelatorioFinanceiro, Long> {
    Optional<RelatorioFinanceiro> findByRelatorioEntrada(RelatorioEntrada entrada);

    Optional<RelatorioFinanceiro> findByRelatorioSaida(RelatorioSaida saida);

    RelatorioFinanceiro findByCodigo(int codigo);
}
