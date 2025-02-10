package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Historico;
import com.shadow.dashboard.models.Parcelas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ParcelasRepository extends JpaRepository<Parcelas, Long> {
    List<Parcelas> findByHistoricoId(Long historicoId);

    List<Parcelas> findByHistorico(Historico historico);


}
