package com.afb.infrastructure.partenaire.adapter.out.persistence;

import jakarta.persistence.*;

@Entity
@Table(name = "partenaires")
public class PartenaireJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private boolean actif = true;

    public PartenaireJpaEntity() {
        // Requis par JPA : Hibernate instancie l'entité par réflexion, puis hydrate ses champs.
    }

    public PartenaireJpaEntity(Long id, String nom, String email, boolean actif) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.actif = actif;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public boolean isActif() { return actif; }
    public void setActif(boolean actif) { this.actif = actif; }
}