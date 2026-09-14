package com.afb.infrastructure.configuration.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationJpaRepository extends JpaRepository<ConfigurationJpaEntity, Long> {
}