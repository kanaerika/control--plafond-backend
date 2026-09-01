package com.afb.domain.partenaire.port.out;

import com.afb.domain.partenaire.model.Partenaire;

import java.util.List;
import java.util.Optional;

/**
 * PORT DE SORTIE : le contrat dont le domaine a besoin pour accéder aux partenaires.
 *
 * Le domaine déclare CE dont il a besoin, sans dire COMMENT c'est réalisé.
 * L'implémentation concrète (avec JPA, R2DBC, ou autre) vit dans l'infrastructure
 * sous forme d'ADAPTATEUR. C'est ce découplage qui rend le domaine indépendant
 * de la base de données.
 */
public interface PartenaireRepositoryPort {

    List<Partenaire> listerTous();

    Optional<Partenaire> trouverParId(Long id);

    boolean existeParNom(String nom);

    /** Enregistre (création ou mise à jour) et retourne le partenaire avec son id. */
    Partenaire enregistrer(Partenaire partenaire);

    void supprimer(Partenaire partenaire);
}
