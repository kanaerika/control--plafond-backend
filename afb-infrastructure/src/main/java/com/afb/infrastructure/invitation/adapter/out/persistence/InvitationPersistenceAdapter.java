package com.afb.infrastructure.invitation.adapter.out.persistence;

import com.afb.domain.invitation.model.Invitation;
import com.afb.domain.invitation.port.out.InvitationTokenPort;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Component
public class InvitationPersistenceAdapter implements InvitationTokenPort {

    private final InvitationJpaRepository jpa;

    public InvitationPersistenceAdapter(InvitationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Invitation enregistrer(Invitation invitation) {
        InvitationJpaEntity e = invitation.getId() != null
                ? jpa.findById(invitation.getId()).orElseGet(InvitationJpaEntity::new)
                : new InvitationJpaEntity();
        e.setEmpreinteToken(invitation.getEmpreinteToken());
        e.setEmail(invitation.getEmail());
        e.setExpiration(invitation.getExpiration());
        e.setUtiliseeLe(invitation.getUtiliseeLe());
        return versDomaine(jpa.save(e));
    }

    @Override
    public Optional<Invitation> trouverParEmpreinte(String empreinteToken) {
        return jpa.findByEmpreinteToken(empreinteToken)
                .map(InvitationPersistenceAdapter::versDomaine);
    }

    /**
     * Un renvoi doit annuler le lien précédent : les invitations encore ouvertes
     * sont marquées consommées plutôt que supprimées, pour garder la trace des
     * envois successifs.
     */
    @Override
    public void invaliderEnAttente(String email) {
        Instant maintenant = Instant.now();
        var enAttente = jpa.findByEmailIgnoreCaseAndUtiliseeLeIsNullAndExpirationAfter(
                email, maintenant);
        enAttente.forEach(e -> e.setUtiliseeLe(maintenant));
        jpa.saveAll(enAttente);
    }

    private static Invitation versDomaine(InvitationJpaEntity e) {
        return new Invitation(e.getId(), e.getEmpreinteToken(), e.getEmail(),
                e.getExpiration(), e.getUtiliseeLe());
    }
}
