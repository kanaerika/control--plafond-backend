package com.afb.domain.agent.model;

import java.util.Objects;

/**
 * Agent : utilisateur opérateur rattaché à un partenaire. Objet métier PUR.
 */
public class Agent {

    private final Long id;
    private String nomComplet;
    private final String email;
    private final String role;          // "AGENT" ou "ADMIN"
    private final Long partenaireId;    // rattachement (isolation)
    private String agence;
    private String codeAgent;
    private boolean actif;
    private final boolean invitationAcceptee;
    private final boolean firstLogin;

    public Agent(Long id, String nomComplet, String email, String role, Long partenaireId,
                 String agence, String codeAgent, boolean actif,
                 boolean invitationAcceptee, boolean firstLogin) {
        this.id = id;
        this.nomComplet = exigerNonVide(nomComplet, "Le nom complet est obligatoire.");
        this.email = exigerNonVide(email, "L'email est obligatoire.").trim().toLowerCase();
        this.role = (role == null || role.isBlank()) ? "AGENT" : role;
        this.partenaireId = partenaireId;
        this.agence = agence;
        this.codeAgent = codeAgent;
        this.actif = actif;
        this.invitationAcceptee = invitationAcceptee;
        this.firstLogin = firstLogin;
    }

    public static Agent creer(String nomComplet, String email, Long partenaireId,
                              String agence, String codeAgent) {
        return new Agent(null, nomComplet, email, "AGENT", partenaireId,
                agence, codeAgent, true, false, true);
    }

    public void modifier(String nouveauNom, String nouvelleAgence, String nouveauCode) {
        this.nomComplet = exigerNonVide(nouveauNom, "Le nom complet est obligatoire.").trim();
        this.agence = nouvelleAgence;
        this.codeAgent = nouveauCode;
    }

    public void basculerActivation() { this.actif = !this.actif; }

    public boolean estAgent() { return "AGENT".equals(role); }
    public boolean estAdmin() { return "ADMIN".equals(role); }

    public String statut() {
        if (!invitationAcceptee) return "Invitation en attente";
        return firstLogin ? "Mot de passe temporaire" : "Actif";
    }

    private static String exigerNonVide(String v, String message) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(message);
        return v;
    }

    public Long getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public Long getPartenaireId() { return partenaireId; }
    public String getAgence() { return agence; }
    public String getCodeAgent() { return codeAgent; }
    public boolean isActif() { return actif; }
    public boolean isInvitationAcceptee() { return invitationAcceptee; }
    public boolean isFirstLogin() { return firstLogin; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Agent autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() { return Objects.hashCode(id); }
}