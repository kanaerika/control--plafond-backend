package com.afb.domain.agent.model;

import java.util.Objects;

/**
 * Agent : utilisateur opérateur rattaché à un partenaire. Objet métier PUR.
 *
 * Construit via {@link #builder()} : l'ancien constructeur à dix paramètres
 * obligeait les appelants à aligner des {@code null, null, true, false, true}
 * dont l'ordre ne se relisait pas (Sonar S107).
 */
public class Agent {

    public static final String ROLE_AGENT = "AGENT";
    public static final String ROLE_ADMIN = "ADMIN";

    private final Long id;
    private String nomComplet;
    private final String email;
    private final String role;
    private final Long partenaireId;    // rattachement (isolation)
    private String agence;
    private String codeAgent;
    private boolean actif;
    private final boolean invitationAcceptee;
    private final boolean firstLogin;

    private Agent(Builder b) {
        this.id = b.id;
        this.nomComplet = exigerNonVide(b.nomComplet, "Le nom complet est obligatoire.");
        this.email = exigerNonVide(b.email, "L'email est obligatoire.").trim().toLowerCase();
        this.role = (b.role == null || b.role.isBlank()) ? ROLE_AGENT : b.role;
        this.partenaireId = b.partenaireId;
        this.agence = b.agence;
        this.codeAgent = b.codeAgent;
        this.actif = b.actif;
        this.invitationAcceptee = b.invitationAcceptee;
        this.firstLogin = b.firstLogin;
    }

    /** Nouveau compte : actif, invitation pas encore acceptée, premier login à venir. */
    public static Builder builder() {
        return new Builder();
    }

    /** Copie modifiable d'un agent existant, pour n'en changer que quelques champs. */
    public Builder copie() {
        return new Builder()
                .id(id).nomComplet(nomComplet).email(email).role(role)
                .partenaireId(partenaireId).agence(agence).codeAgent(codeAgent)
                .actif(actif).invitationAcceptee(invitationAcceptee).firstLogin(firstLogin);
    }

    public static Agent creer(String nomComplet, String email, Long partenaireId,
                              String agence, String codeAgent) {
        return builder()
                .nomComplet(nomComplet).email(email).role(ROLE_AGENT)
                .partenaireId(partenaireId).agence(agence).codeAgent(codeAgent)
                .build();
    }

    public void modifier(String nouveauNom, String nouvelleAgence, String nouveauCode) {
        this.nomComplet = exigerNonVide(nouveauNom, "Le nom complet est obligatoire.").trim();
        this.agence = nouvelleAgence;
        this.codeAgent = nouveauCode;
    }

    public void basculerActivation() { this.actif = !this.actif; }

    public boolean estAgent() { return ROLE_AGENT.equals(role); }
    public boolean estAdmin() { return ROLE_ADMIN.equals(role); }

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

    /**
     * Les valeurs par défaut sont celles d'un compte tout juste créé ; les règles
     * (nom et email obligatoires, rôle AGENT par défaut) sont appliquées à la
     * construction, quel que soit le chemin emprunté.
     */
    public static final class Builder {
        private Long id;
        private String nomComplet;
        private String email;
        private String role = ROLE_AGENT;
        private Long partenaireId;
        private String agence;
        private String codeAgent;
        private boolean actif = true;
        private boolean invitationAcceptee = false;
        private boolean firstLogin = true;

        private Builder() {
            // Instancié uniquement via Agent.builder() ou Agent#copie().
        }

        public Builder id(Long v) { this.id = v; return this; }
        public Builder nomComplet(String v) { this.nomComplet = v; return this; }
        public Builder email(String v) { this.email = v; return this; }
        public Builder role(String v) { this.role = v; return this; }
        public Builder partenaireId(Long v) { this.partenaireId = v; return this; }
        public Builder agence(String v) { this.agence = v; return this; }
        public Builder codeAgent(String v) { this.codeAgent = v; return this; }
        public Builder actif(boolean v) { this.actif = v; return this; }
        public Builder invitationAcceptee(boolean v) { this.invitationAcceptee = v; return this; }
        public Builder firstLogin(boolean v) { this.firstLogin = v; return this; }

        public Agent build() { return new Agent(this); }
    }
}
