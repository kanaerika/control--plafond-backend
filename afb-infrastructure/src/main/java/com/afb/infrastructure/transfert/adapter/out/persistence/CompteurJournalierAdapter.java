package com.afb.infrastructure.transfert.adapter.out.persistence;

import com.afb.domain.transfert.model.CompteursDuJour;
import com.afb.domain.transfert.port.out.CompteurJournalierPort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDate;
import java.util.function.Consumer;
import com.afb.domain.transfert.model.CompteursDuJour;
@Component
public class CompteurJournalierAdapter implements CompteurJournalierPort {

    private final CompteurJournalierJpaRepository repo;

    public CompteurJournalierAdapter(CompteurJournalierJpaRepository repo) {
        this.repo = repo;
    }

    @Override
    @Transactional
    public void incrementerRejetes(Long agentId) {
        maj(agentId, c -> c.setRejetes(c.getRejetes() + 1));
    }

    @Override
    @Transactional
    public void incrementerNonClotures(Long agentId) {
        maj(agentId, c -> c.setNonClotures(c.getNonClotures() + 1));
    }

    @Override
    @Transactional
    public void incrementerExecutes(Long agentId) {
        maj(agentId, c -> c.setExecutes(c.getExecutes() + 1));
    }

    @Override
    @Transactional
    public void incrementerAnnules(Long agentId) {
        maj(agentId, c -> c.setAnnules(c.getAnnules() + 1));
    }

    @Override
@Transactional
public void basculerNonClotureVersExecute(Long agentId, LocalDate jour) {
    CompteurJournalierJpaEntity c = repo.findByAgentIdAndJour(agentId, jour)
            .orElseGet(() -> new CompteurJournalierJpaEntity(agentId, jour));
    c.setNonClotures(Math.max(0, c.getNonClotures() - 1));
    c.setExecutes(c.getExecutes() + 1);
    repo.save(c);
}
     @Override
@Transactional
public void basculerNonClotureVersRejete(Long agentId, LocalDate jour) {
    CompteurJournalierJpaEntity c = repo.findByAgentIdAndJour(agentId, jour)
            .orElseGet(() -> new CompteurJournalierJpaEntity(agentId, jour));
    c.setNonClotures(Math.max(0, c.getNonClotures() - 1));
    c.setRejetes(c.getRejetes() + 1);
    repo.save(c);
}

    @Override
@Transactional(readOnly = true)
public CompteursDuJour lire(Long agentId, LocalDate jour) {
    return repo.findByAgentIdAndJour(agentId, jour)
            .map(c -> new CompteursDuJour(c.getExecutes(), c.getRejetes(),
                    c.getAnnules(), c.getNonClotures()))
            .orElseGet(CompteursDuJour::vide);
}
    private void maj(Long agentId, Consumer<CompteurJournalierJpaEntity> action) {
        LocalDate jour = LocalDate.now(Clock.systemDefaultZone());
        CompteurJournalierJpaEntity c = repo.findByAgentIdAndJour(agentId, jour)
                .orElseGet(() -> new CompteurJournalierJpaEntity(agentId, jour));
        action.accept(c);
        repo.save(c);
    }
}