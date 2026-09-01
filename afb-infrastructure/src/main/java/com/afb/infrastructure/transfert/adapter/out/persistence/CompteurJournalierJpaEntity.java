package com.afb.infrastructure.transfert.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "compteurs_journaliers",
       uniqueConstraints = @UniqueConstraint(columnNames = {"agent_id", "jour"}))
public class CompteurJournalierJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(nullable = false)
    private LocalDate jour;

    private int executes;
    private int rejetes;
    private int annules;
    private int nonClotures;

    public CompteurJournalierJpaEntity() {}

    public CompteurJournalierJpaEntity(Long agentId, LocalDate jour) {
        this.agentId = agentId;
        this.jour = jour;
    }

    public Long getId() { return id; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long v) { this.agentId = v; }
    public LocalDate getJour() { return jour; }
    public void setJour(LocalDate v) { this.jour = v; }
    public int getExecutes() { return executes; }
    public void setExecutes(int v) { this.executes = v; }
    public int getRejetes() { return rejetes; }
    public void setRejetes(int v) { this.rejetes = v; }
    public int getAnnules() { return annules; }
    public void setAnnules(int v) { this.annules = v; }
    public int getNonClotures() { return nonClotures; }
    public void setNonClotures(int v) { this.nonClotures = v; }
}