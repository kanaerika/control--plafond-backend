package com.afb.infrastructure.agent.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "agents")
public class AgentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomComplet;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String role = "AGENT";

    @Column(name = "partenaire_id")
    private Long partenaireId;

    private String motDePasse;

    @Column(nullable = false)
    private boolean actif = true;

    @Column(nullable = false)
    private boolean invitationAcceptee = false;

    @Column(nullable = false)
    private boolean firstLogin = true;

    private String tokenInvitation;
    private Instant tokenExpiration;
    private String codeAgent;
    private String agence;
    private String telephone;

    public AgentJpaEntity() {
        // Requis par JPA : Hibernate instancie l'entité par réflexion, puis hydrate ses champs.
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String v) { this.nomComplet = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
    public Long getPartenaireId() { return partenaireId; }
    public void setPartenaireId(Long v) { this.partenaireId = v; }
    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String v) { this.motDePasse = v; }
    public boolean isActif() { return actif; }
    public void setActif(boolean v) { this.actif = v; }
    public boolean isInvitationAcceptee() { return invitationAcceptee; }
    public void setInvitationAcceptee(boolean v) { this.invitationAcceptee = v; }
    public boolean isFirstLogin() { return firstLogin; }
    public void setFirstLogin(boolean v) { this.firstLogin = v; }
    public String getTokenInvitation() { return tokenInvitation; }
    public void setTokenInvitation(String v) { this.tokenInvitation = v; }
    public Instant getTokenExpiration() { return tokenExpiration; }
    public void setTokenExpiration(Instant v) { this.tokenExpiration = v; }
    public String getCodeAgent() { return codeAgent; }
    public void setCodeAgent(String v) { this.codeAgent = v; }
    public String getAgence() { return agence; }
    public void setAgence(String v) { this.agence = v; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String v) { this.telephone = v; }
}