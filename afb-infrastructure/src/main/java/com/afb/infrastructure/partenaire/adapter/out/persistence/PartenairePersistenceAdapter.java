package com.afb.infrastructure.partenaire.adapter.out.persistence;

import com.afb.domain.partenaire.model.Partenaire;
import com.afb.domain.partenaire.port.out.PartenaireRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class PartenairePersistenceAdapter implements PartenaireRepositoryPort {

    private final PartenaireJpaRepository jpa;

    public PartenairePersistenceAdapter(PartenaireJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Partenaire> listerTous() {
        return jpa.findAll().stream().map(PartenairePersistenceAdapter::versDomaine).toList();
    }

    @Override
    public Optional<Partenaire> trouverParId(Long id) {
        return jpa.findById(id).map(PartenairePersistenceAdapter::versDomaine);
    }

    @Override
    public boolean existeParNom(String nom) {
        return jpa.existsByNomIgnoreCase(nom);
    }

    @Override
    public Partenaire enregistrer(Partenaire partenaire) {
        PartenaireJpaEntity sauve = jpa.save(versEntite(partenaire));
        return versDomaine(sauve);
    }

    @Override
    public void supprimer(Partenaire partenaire) {
        jpa.deleteById(partenaire.getId());
    }

    private static Partenaire versDomaine(PartenaireJpaEntity e) {
        return new Partenaire(e.getId(), e.getNom(), e.getEmail(), e.isActif());
    }

    private static PartenaireJpaEntity versEntite(Partenaire p) {
        return new PartenaireJpaEntity(p.getId(), p.getNom(), p.getEmail(), p.isActif());
    }
}