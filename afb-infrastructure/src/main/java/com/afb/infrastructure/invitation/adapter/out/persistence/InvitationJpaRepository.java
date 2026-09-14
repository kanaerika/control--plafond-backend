package com.afb.infrastructure.invitation.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface InvitationJpaRepository extends JpaRepository<InvitationJpaEntity, Long> {

    Optional<InvitationJpaEntity> findByEmpreinteToken(String empreinteToken);

    List<InvitationJpaEntity> findByEmailIgnoreCaseAndUtiliseeLeIsNullAndExpirationAfter(
            String email, Instant maintenant);
}
