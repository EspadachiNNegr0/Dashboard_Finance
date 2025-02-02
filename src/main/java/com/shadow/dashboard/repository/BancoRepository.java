package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Banco;
import com.shadow.dashboard.models.Historico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BancoRepository extends JpaRepository<Banco, Long> {

    @Override
    Optional<Banco> findById(Long aLong);

    Long id(Long id);
}
