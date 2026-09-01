package com.afb.domain.partenaire.model;

import java.util.Objects;

/**
 * Partenaire de distribution (Financial House, Express Union, Julie Voyage...).
 * Objet métier PUR : aucune dépendance technique (ni JPA, ni Spring).
 */
public class Partenaire {

    private final Long id;
    private String nom;
    private String email;
    private boolean actif;

    public Partenaire(Long id, String nom, String email, boolean actif) {
        this.id = id;
        this.nom = exigerNonVide(nom, "Le nom du partenaire est obligatoire.");
        this.email = exigerNonVide(email, "L'email du partenaire est obligatoire.");
        this.actif = actif;
    }

    public static Partenaire creer(String nom, String email) {
        return new Partenaire(null, nom.trim(), email.trim().toLowerCase(), true);
    }

    public void activer()   { this.actif = true; }
    public void desactiver() { this.actif = false; }
    public void basculerActivation() { this.actif = !this.actif; }

    public void modifier(String nouveauNom, String nouvelEmail) {
        this.nom = exigerNonVide(nouveauNom, "Le nom du partenaire est obligatoire.").trim();
        this.email = exigerNonVide(nouvelEmail, "L'email du partenaire est obligatoire.")
                .trim().toLowerCase();
    }

    private static String exigerNonVide(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return valeur;
    }

    public Long getId()      { return id; }
    public String getNom()   { return nom; }
    public String getEmail() { return email; }
    public boolean isActif() { return actif; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Partenaire autre)) return false;
        return id != null && id.equals(autre.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}