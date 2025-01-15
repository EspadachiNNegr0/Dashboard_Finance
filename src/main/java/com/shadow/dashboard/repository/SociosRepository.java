package com.shadow.dashboard.repository;

import com.shadow.dashboard.models.Socios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SociosRepository extends JpaRepository<Socios, Long> {

}
