package com.afb.infrastructure.agent.adapter.out.persistence;

import com.afb.domain.agent.exception.AgentAvecHistorique;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class AgentPersistenceAdapter implements AgentRepositoryPort {

    private final AgentJpaRepository jpa;

    public AgentPersistenceAdapter(AgentJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Agent> listerParPartenaire(Long partenaireId) {
        return jpa.findByPartenaireIdOrderByNomComplet(partenaireId).stream()
                .map(AgentPersistenceAdapter::versDomaine).toList();
    }

    @Override
    public Optional<Agent> trouverParId(Long id) {
        return jpa.findById(id).map(AgentPersistenceAdapter::versDomaine);
    }

    @Override
    public Optional<Agent> trouverParEmail(String email) {
        return jpa.findByEmailIgnoreCase(email).map(AgentPersistenceAdapter::versDomaine);
    }

    @Override
    public boolean existeParEmail(String email) {
        return jpa.existsByEmailIgnoreCase(email);
    }

    @Override
    public Agent enregistrer(Agent agent) {
        AgentJpaEntity e = agent.getId() != null
                ? jpa.findById(agent.getId()).orElseGet(AgentJpaEntity::new)
                : new AgentJpaEntity();
        appliquer(agent, e);
        return versDomaine(jpa.save(e));
    }

    /**
     * Le flush force la contrainte de clé étrangère à se déclencher ici plutôt
     * qu'au commit : sans lui, la violation remonterait après la sortie du cas
     * d'usage et le message SQL brut de Postgres finirait à l'écran.
     */
    @Override
    public void supprimer(Agent agent) {
        try {
            jpa.deleteById(agent.getId());
            jpa.flush();
        } catch (DataIntegrityViolationException e) {
            throw new AgentAvecHistorique(agent.getNomComplet(), e);
        }
    }

    @Override
    public void marquerInvitationAcceptee(String email) {
        jpa.findByEmailIgnoreCase(email).ifPresent(e -> {
            e.setInvitationAcceptee(true);
            e.setFirstLogin(false);
            jpa.save(e);
        });
    }

    private static Agent versDomaine(AgentJpaEntity e) {
        return Agent.builder()
                .id(e.getId()).nomComplet(e.getNomComplet()).email(e.getEmail()).role(e.getRole())
                .partenaireId(e.getPartenaireId()).agence(e.getAgence()).codeAgent(e.getCodeAgent())
                .actif(e.isActif()).invitationAcceptee(e.isInvitationAcceptee())
                .firstLogin(e.isFirstLogin())
                .build();
    }

    private static void appliquer(Agent a, AgentJpaEntity e) {
        e.setNomComplet(a.getNomComplet());
        e.setEmail(a.getEmail());
        e.setRole(a.getRole());
        e.setPartenaireId(a.getPartenaireId());
        e.setAgence(a.getAgence());
        e.setCodeAgent(a.getCodeAgent());
        e.setActif(a.isActif());
    }
}