package com.afb.infrastructure.statistique.adapter.out.persistence;

import com.afb.domain.statistique.port.out.StatistiquesPort;
import com.afb.domain.transfert.model.StatutTransfert;
import com.afb.infrastructure.agent.adapter.out.persistence.AgentJpaRepository;
import com.afb.infrastructure.partenaire.adapter.out.persistence.PartenaireJpaRepository;
import com.afb.infrastructure.transfert.adapter.out.persistence.TransfertJpaEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class StatistiquesAdapter implements StatistiquesPort {

    private final StatistiquesJpaRepository repo;
    private final AgentJpaRepository agents;
    private final PartenaireJpaRepository partenaires;

    public StatistiquesAdapter(StatistiquesJpaRepository repo,
                               AgentJpaRepository agents,
                               PartenaireJpaRepository partenaires) {
        this.repo = repo;
        this.agents = agents;
        this.partenaires = partenaires;
    }

    @Override public long compterParPartenaire(Long partenaireId) {
        return repo.countByPartenaireId(partenaireId);
    }

    @Override public long compterParAgentEntreDates(Long agentId, LocalDate debut, LocalDate fin) {
        return repo.countByAgentIdAndDateTransfertBetween(agentId, debut, fin);
    }

    @Override public long compterParAgentEtStatut(Long agentId, String statut) {
        return repo.countByAgentIdAndStatut(agentId, StatutTransfert.valueOf(statut));
    }

    @Override public List<Object[]> repartitionParStatutPartenaire(Long partenaireId) {
        return repo.repartitionParStatutPartenaire(partenaireId);
    }

    @Override public List<Object[]> repartitionParStatutAgent(Long agentId) {
        return repo.repartitionParStatutAgent(agentId);
    }

    @Override public List<Object[]> repartitionParStatutPlateforme() {
        return repo.repartitionParStatutPlateforme();
    }

    @Override public List<Object[]> parMoisPartenaire(Long partenaireId, LocalDate depuis) {
        return repo.parMoisPartenaire(partenaireId, depuis);
    }

    @Override public List<Object[]> parMoisAgent(Long agentId, LocalDate depuis) {
        return repo.parMoisAgent(agentId, depuis);
    }

    @Override public List<Object[]> parMoisPlateforme(LocalDate depuis) {
        return repo.parMoisPlateforme(depuis);
    }

    /** Résout le nom de chaque agent pour le classement. */
    @Override public List<Object[]> topAgents(Long partenaireId, int limite) {
        List<Object[]> bruts = repo.topAgents(partenaireId, PageRequest.of(0, limite));
        List<Object[]> res = new ArrayList<>();
        for (Object[] l : bruts) {
            Long agentId = ((Number) l[0]).longValue();
            long count = ((Number) l[1]).longValue();
            String nom = agents.findById(agentId)
                    .map(a -> a.getNomComplet()).orElse("—");
            res.add(new Object[]{ agentId, nom, count });
        }
        return res;
    }

    @Override public List<Object[]> topPartenaires(int limite) {
        List<Object[]> bruts = repo.topPartenaires(PageRequest.of(0, limite));
        List<Object[]> res = new ArrayList<>();
        for (Object[] l : bruts) {
            Long pid = ((Number) l[0]).longValue();
            long count = ((Number) l[1]).longValue();
            String nom = partenaires.findById(pid)
                    .map(p -> p.getNom()).orElse("—");
            res.add(new Object[]{ pid, nom, count });
        }
        return res;
    }

    @Override public List<Object[]> activiteRecentePartenaire(Long partenaireId) {
        return versActivite(repo.findTop8ByPartenaireIdOrderByIdDesc(partenaireId));
    }

    @Override public List<Object[]> activiteRecenteAgent(Long agentId) {
        return versActivite(repo.findTop8ByAgentIdOrderByIdDesc(agentId));
    }

    @Override public List<Object[]> activiteRecentePlateforme() {
        return versActivite(repo.findTop8ByOrderByIdDesc());
    }

    /** [id, nomClient, montant, statut, date, agentNom] */
    private List<Object[]> versActivite(List<TransfertJpaEntity> liste) {
        List<Object[]> res = new ArrayList<>();
        for (TransfertJpaEntity t : liste) {
            String agentNom = t.getAgentId() == null ? "—"
                    : agents.findById(t.getAgentId()).map(a -> a.getNomComplet()).orElse("—");
            res.add(new Object[]{
                    t.getId(),
                    t.getNomClient(),
                    t.getMontant(),
                    t.getStatut() == null ? "" : t.getStatut().name(),
                    t.getDateTransfert() == null ? "" : t.getDateTransfert().toString(),
                    agentNom
            });
        }
        return res;
    }
}