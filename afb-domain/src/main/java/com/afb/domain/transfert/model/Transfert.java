package com.afb.domain.transfert.model;

import java.time.LocalDate;

public class Transfert {

    private Long id;
    private String nomClient;
    private String dateNaissance;
    private String naturePiece;
    private String numeroPiece;
    private long montant;
    private String paysDestination;
    private StatutTransfert statut;
    private String reference;
    private String referenceVerification;
    private String motif;
    private String agence;
    private String canal;
    private LocalDate dateTransfert;
    private long cumulMois;
    private Long partenaireId;
    private Long agentId;

    public Transfert() {}

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