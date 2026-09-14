package com.afb.domain.invitation.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Invitation émise par la plateforme : un lien à usage unique, valable un temps
 * limité, qui permet à un agent ou à un administrateur de partenaire de définir
 * son mot de passe avant sa première connexion.
 *
 * Le jeton en clair n'existe que le temps de construire l'email : seule son
 * empreinte est conservée ({@code empreinteToken}). Quelqu'un qui lirait la base
 * ne peut donc pas rejouer une invitation en attente.
 */
public class Invitation {

    /** Durée de validité d'un lien d'invitation. */
    public static final long VALIDITE_HEURES = 48;

    private final Long id;
    private final String empreinteToken;
    private final String email;
    private final Instant expiration;
    private Instant utiliseeLe;

    public Invitation(Long id, String empreinteToken, String email,
                      Instant expiration, Instant utiliseeLe) {
        this.id = id;
        this.empreinteToken = exigerNonVide(empreinteToken, "L'empreinte du jeton est obligatoire.");
        this.email = exigerNonVide(email, "L'email est obligatoire.").trim().toLowerCase();
        this.expiration = Objects.requireNonNull(expiration, "L'expiration est obligatoire.");
        this.utiliseeLe = utiliseeLe;
    }

    public static Invitation emettre(String empreinteToken, String email, Instant maintenant) {
        return new Invitation(null, empreinteToken, email,
                maintenant.plusSeconds(VALIDITE_HEURES * 3600), null);
    }

    public boolean estUtilisee() { return utiliseeLe != null; }

    public boolean estExpiree(Instant maintenant) { return maintenant.isAfter(expiration); }

    /** Utilisable une seule fois : au-delà, le lien ne vaut plus rien. */
    public boolean estUtilisable(Instant maintenant) {
        return !estUtilisee() && !estExpiree(maintenant);
    }

    public void marquerUtilisee(Instant maintenant) { this.utiliseeLe = maintenant; }

    public Long getId() { return id; }
    public String getEmpreinteToken() { return empreinteToken; }
    public String getEmail() { return email; }
    public Instant getExpiration() { return expiration; }
    public Instant getUtiliseeLe() { return utiliseeLe; }

    private static String exigerNonVide(String v, String message) {
        if (v == null || v.isBlank()) throw new IllegalArgumentException(message);
        return v;
    }
}
