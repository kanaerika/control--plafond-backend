package com.afb.application.partenaire.usecase;

import com.afb.application.partenaire.port.in.GererPartenairesUseCase;
import com.afb.application.partenaire.port.in.ModifierPartenaireCommande;
import com.afb.application.partenaire.port.in.PartenaireResultat;
import com.afb.domain.partenaire.exception.NomPartenaireDejaUtilise;
import com.afb.domain.partenaire.exception.PartenaireNonTrouve;
import com.afb.domain.partenaire.model.Partenaire;
import com.afb.domain.partenaire.port.out.PartenaireRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GererPartenairesService implements GererPartenairesUseCase {

    private final PartenaireRepositoryPort partenaires;

    public GererPartenairesService(PartenaireRepositoryPort partenaires) {
        this.partenaires = partenaires;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartenaireResultat> lister() {
        return partenaires.listerTous().stream()
                .map(GererPartenairesService::versResultat)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PartenaireResultat detail(Long id) {
        return versResultat(chargerOuEchouer(id));
    }

    @Override
    @Transactional
    public PartenaireResultat modifier(ModifierPartenaireCommande commande) {
        Partenaire partenaire = chargerOuEchouer(commande.id());

        boolean nomChange = !partenaire.getNom().equalsIgnoreCase(commande.nom().trim());
        if (nomChange && partenaires.existeParNom(commande.nom().trim())) {
            throw new NomPartenaireDejaUtilise(commande.nom().trim());
        }

        partenaire.modifier(commande.nom(), commande.email());
        return versResultat(partenaires.enregistrer(partenaire));
    }

    @Override
    @Transactional
    public PartenaireResultat basculerActivation(Long id) {
        Partenaire partenaire = chargerOuEchouer(id);
        partenaire.basculerActivation();
        return versResultat(partenaires.enregistrer(partenaire));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        Partenaire partenaire = chargerOuEchouer(id);
        partenaires.supprimer(partenaire);
    }

    private Partenaire chargerOuEchouer(Long id) {
        return partenaires.trouverParId(id)
                .orElseThrow(() -> new PartenaireNonTrouve(id));
    }

    private static PartenaireResultat versResultat(Partenaire p) {
        String statut = p.isActif() ? "Actif" : "Désactivé";
        return new PartenaireResultat(p.getId(), p.getNom(), p.getEmail(), p.isActif(), statut);
    }
}