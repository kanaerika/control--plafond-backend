package com.afb.infrastructure.agent.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AgentJpaRepository extends JpaRepository<AgentJpaEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
    List<AgentJpaEntity> findByPartenaireIdOrderByNomComplet(Long partenaireId);
}