package com.afb.infrastructure.transfert.adapter.out.persistence;

import com.afb.domain.transfert.model.StatutTransfert;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "transferts", indexes = {
        @Index(name = "idx_transfert_client", columnList = "nomClient"),
        @Index(name = "idx_transfert_date", columnList = "dateTransfert")
})
public class TransfertJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomClient;

    private String dateNaissance;
    private String naturePiece;
    private String numeroPiece;

    @Column(nullable = false)
    private long montant;

    @Column(nullable = false)
    private String paysDestination;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutTransfert statut = StatutTransfert.EXECUTE;

    private String reference;
    private String referenceVerification;

    @Column(length = 300)
    private String motif;

    private String agence;
    private String canal;

    @Column(nullable = false)
    private LocalDate dateTransfert;

    private long cumulMois;

    @Column(name = "partenaire_id")
    private Long partenaireId;

    @Column(name = "agent_id")
    private Long agentId;

    public TransfertJpaEntity() {
        // Requis par JPA : Hibernate instancie l'entité par réflexion, puis hydrate ses champs.
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomClient() { return nomClient; }
    public void setNomClient(String v) { this.nomClient = v; }
    public String getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(String v) { this.dateNaissance = v; }
    public String getNaturePiece() { return naturePiece; }
    public void setNaturePiece(String v) { this.naturePiece = v; }
    public String getNumeroPiece() { return numeroPiece; }
    public void setNumeroPiece(String v) { this.numeroPiece = v; }
    public long getMontant() { return montant; }
    public void setMontant(long v) { this.montant = v; }
    public String getPaysDestination() { return paysDestination; }
    public void setPaysDestination(String v) { this.paysDestination = v; }
    public StatutTransfert getStatut() { return statut; }
    public void setStatut(StatutTransfert v) { this.statut = v; }
    public String getReference() { return reference; }
    public void setReference(String v) { this.reference = v; }
    public String getReferenceVerification() { return referenceVerification; }
    public void setReferenceVerification(String v) { this.referenceVerification = v; }
    public String getMotif() { return motif; }
    public void setMotif(String v) { this.motif = v; }
    public String getAgence() { return agence; }
    public void setAgence(String v) { this.agence = v; }
    public String getCanal() { return canal; }
    public void setCanal(String v) { this.canal = v; }
    public LocalDate getDateTransfert() { return dateTransfert; }
    public void setDateTransfert(LocalDate v) { this.dateTransfert = v; }
    public long getCumulMois() { return cumulMois; }
    public void setCumulMois(long v) { this.cumulMois = v; }
    public Long getPartenaireId() { return partenaireId; }
    public void setPartenaireId(Long v) { this.partenaireId = v; }
    public Long getAgentId() { return agentId; }
    public void setAgentId(Long v) { this.agentId = v; }
}
