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
    private final com.afb.domain.agent.port.out.AgentRepositoryPort agents;
    private final com.afb.domain.agent.port.out.InvitationPort invitations;
    public GererPartenairesService(PartenaireRepositoryPort partenaires,
                                   com.afb.domain.agent.port.out.AgentRepositoryPort agents,
                                   com.afb.domain.agent.port.out.InvitationPort invitations) {
        this.partenaires = partenaires;
        this.agents = agents;
        this.invitations = invitations;
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
        // Retire d'abord les agents rattachés : la table agents référence
        // partenaires par clé étrangère, et chaque partenaire est créé avec
        // au moins son administrateur.
        agents.listerParPartenaire(id).forEach(agents::supprimer);
        partenaires.supprimer(partenaire);
    }

    // En écriture : l'émission d'une invitation enregistre désormais un jeton
    // (table « invitations ») en plus d'appeler le service d'authentification.
    @Override
    @Transactional
    public String renvoyerInvitation(Long id) {
        Partenaire partenaire = chargerOuEchouer(id);
        // Le message s'adresse à une personne : on reprend le nom de
        // l'administrateur, pas la raison sociale du partenaire.
        String nomAdmin = agents.trouverParEmail(partenaire.getEmail())
                .map(com.afb.domain.agent.model.Agent::getNomComplet)
                .orElse(partenaire.getNom());
        invitations.renvoyerInvitation(partenaire.getEmail(), nomAdmin, "ADMIN");
        return "Invitation renvoyée à l'administrateur de « " + partenaire.getNom() + " ».";
    }

    private Partenaire chargerOuEchouer(Long id) {
        return partenaires.trouverParId(id)
                .orElseThrow(() -> new PartenaireNonTrouve(id));
    }

    private static PartenaireResultat versResultat(Partenaire p) {
        String statut = p.isActif() ? "Actif" : "Désactivé";
        return new PartenaireResultat(p.getId(), p.getNom(), p.getEmail(), p.isActif(), statut);
    }

    @Override
@Transactional
public PartenaireResultat creer(com.afb.application.partenaire.port.in.CreerPartenaireCommande cmd) {
    String nom = cmd.nom().trim();
    String email = cmd.email().trim().toLowerCase();

    if (partenaires.existeParNom(nom)) {
        throw new NomPartenaireDejaUtilise(nom);
    }
    if (agents.existeParEmail(email)) {
        throw new com.afb.domain.agent.exception.EmailAgentDejaUtilise(email);
    }

    // 1. Crée le partenaire
    Partenaire p = partenaires.enregistrer(Partenaire.creer(nom, email));

    // 2. Crée son administrateur (rôle ADMIN) dans Keycloak + invitation.
    //    Le realm Keycloak ne connaît que ADMIN / AGENT ; la distinction
    //    « admin Afriland » se fait sur le partenaire d'appartenance.
    com.afb.domain.agent.model.Agent admin = new com.afb.domain.agent.model.Agent(
            null, cmd.nomAdministrateur().trim(), email, "ADMIN",
            p.getId(), null, null, true, false, true);
    invitations.creerEtInviter(admin, "ADMIN");

    // 3. Enregistre l'admin dans la base métier
    agents.enregistrer(admin);

    return versResultat(p);
}
}