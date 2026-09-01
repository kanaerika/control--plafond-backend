package com.afb.infrastructure.partenaire.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PartenaireJpaRepository extends JpaRepository<PartenaireJpaEntity, Long> {
    boolean existsByNomIgnoreCase(String nom);
}