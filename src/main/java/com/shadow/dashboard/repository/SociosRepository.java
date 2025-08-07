package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Socios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SociosRepository extends JpaRepository<Socios, Long> {

    Optional<Socios> findByName(String name);
}
