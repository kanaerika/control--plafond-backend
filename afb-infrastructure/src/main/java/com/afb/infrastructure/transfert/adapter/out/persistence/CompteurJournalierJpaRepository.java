package com.afb.infrastructure.transfert.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface CompteurJournalierJpaRepository extends JpaRepository<CompteurJournalierJpaEntity, Long> {
    Optional<CompteurJournalierJpaEntity> findByAgentIdAndJour(Long agentId, LocalDate jour);
}