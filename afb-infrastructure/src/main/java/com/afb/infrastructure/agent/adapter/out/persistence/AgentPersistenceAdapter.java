package com.afb.infrastructure.agent.adapter.out.persistence;

import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
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

    @Override
    public void supprimer(Agent agent) {
        jpa.deleteById(agent.getId());
    }

    private static Agent versDomaine(AgentJpaEntity e) {
        return new Agent(e.getId(), e.getNomComplet(), e.getEmail(), e.getRole(),
                e.getPartenaireId(), e.getAgence(), e.getCodeAgent(), e.isActif(),
                e.isInvitationAcceptee(), e.isFirstLogin());
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