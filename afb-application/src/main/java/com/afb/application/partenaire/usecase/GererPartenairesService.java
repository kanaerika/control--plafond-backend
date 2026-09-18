package com.afb.application.partenaire.usecase;

import com.afb.application.partenaire.port.in.CreerPartenaireCommande;
import com.afb.application.partenaire.port.in.GererPartenairesUseCase;
import com.afb.application.partenaire.port.in.ModifierPartenaireCommande;
import com.afb.application.partenaire.port.in.PartenaireResultat;
import com.afb.domain.agent.exception.AgentAvecHistorique;
import com.afb.domain.agent.exception.EmailAgentDejaUtilise;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.AgentRepositoryPort;
import com.afb.domain.agent.port.out.InvitationPort;
import com.afb.domain.partenaire.exception.AccesReserveAfriland;
import com.afb.domain.partenaire.exception.NomPartenaireDejaUtilise;
import com.afb.domain.partenaire.exception.PartenaireAvecHistorique;
import com.afb.domain.partenaire.exception.PartenaireNonTrouve;
import com.afb.domain.partenaire.model.Partenaire;
import com.afb.domain.partenaire.port.out.PartenaireRepositoryPort;
import com.afb.domain.statistique.port.out.UtilisateurCourantPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des partenaires : réservée à l'administration d'Afriland (SUPER_ADMIN).
 *
 * Le contrôle est fait ici et pas seulement dans le front : le rôle Keycloak
 * « ADMIN » est porté par tous les administrateurs de partenaire, et l'API
 * restait appelable directement par n'importe lequel d'entre eux.
 */
@Service
public class GererPartenairesService implements GererPartenairesUseCase {

    private final PartenaireRepositoryPort partenaires;
    private final AgentRepositoryPort agents;
    private final InvitationPort invitations;
    private final UtilisateurCourantPort utilisateur;

    public GererPartenairesService(PartenaireRepositoryPort partenaires,
                                   AgentRepositoryPort agents,
                                   InvitationPort invitations,
                                   UtilisateurCourantPort utilisateur) {
        this.partenaires = partenaires;
        this.agents = agents;
        this.invitations = invitations;
        this.utilisateur = utilisateur;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PartenaireResultat> lister() {
        exigerAfriland();
        return partenaires.listerTous().stream()
                .map(GererPartenairesService::versResultat)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PartenaireResultat detail(Long id) {
        exigerAfriland();
        return versResultat(chargerOuEchouer(id));
    }

    @Override
    @Transactional
    public PartenaireResultat creer(CreerPartenaireCommande cmd) {
        exigerAfriland();
        String nom = obligatoire(cmd.nom(), "Le nom du partenaire est obligatoire.");
        String email = obligatoire(cmd.email(), "L'email de l'administrateur est obligatoire.")
                .toLowerCase();
        String nomAdmin = obligatoire(cmd.nomAdministrateur(),
                "Le nom de l'administrateur est obligatoire.");

        if (partenaires.existeParNom(nom)) {
            throw new NomPartenaireDejaUtilise(nom);
        }
        if (agents.existeParEmail(email)) {
            throw new EmailAgentDejaUtilise(email);
        }

        Partenaire p = partenaires.enregistrer(Partenaire.creer(nom, email));

        // Le realm Keycloak ne connaît que ADMIN / AGENT ; la distinction
        // « admin Afriland » se fait sur le partenaire d'appartenance.
        Agent admin = Agent.builder()
                .nomComplet(nomAdmin).email(email).role(Agent.ROLE_ADMIN).partenaireId(p.getId())
                .build();
        invitations.creerEtInviter(admin, Agent.ROLE_ADMIN);
        agents.enregistrer(admin);

        return versResultat(p);
    }

    @Override
    @Transactional
    public PartenaireResultat modifier(ModifierPartenaireCommande commande) {
        exigerAfriland();
        Partenaire partenaire = chargerOuEchouer(commande.id());
        String nouveauNom = obligatoire(commande.nom(), "Le nom du partenaire est obligatoire.");

        boolean nomChange = !partenaire.getNom().equalsIgnoreCase(nouveauNom);
        if (nomChange && partenaires.existeParNom(nouveauNom)) {
            throw new NomPartenaireDejaUtilise(nouveauNom);
        }

        partenaire.modifier(nouveauNom, commande.email());
        return versResultat(partenaires.enregistrer(partenaire));
    }

    @Override
    @Transactional
    public PartenaireResultat basculerActivation(Long id) {
        exigerAfriland();
        Partenaire partenaire = chargerOuEchouer(id);
        exigerAutreQueLeSien(partenaire, "désactiver");
        partenaire.basculerActivation();
        return versResultat(partenaires.enregistrer(partenaire));
    }

    @Override
    @Transactional
    public void supprimer(Long id) {
        exigerAfriland();
        Partenaire partenaire = chargerOuEchouer(id);
        exigerAutreQueLeSien(partenaire, "supprimer");

        // Les agents d'abord : la table agents référence partenaires par clé
        // étrangère. Un agent qui a de l'historique bloque la suppression ; le
        // message doit alors parler du partenaire, pas de cet agent-là.
        List<Agent> rattaches = agents.listerParPartenaire(id);
        try {
            rattaches.forEach(agents::supprimer);
        } catch (AgentAvecHistorique e) {
            throw new PartenaireAvecHistorique(partenaire.getNom());
        }
        partenaires.supprimer(partenaire);

        // Sans quoi les adresses resteraient prises dans Keycloak et ne
        // pourraient plus jamais servir à créer un compte.
        rattaches.forEach(a -> invitations.supprimerCompte(a.getEmail()));
    }

    // En écriture : l'émission d'une invitation enregistre un jeton (table
    // « invitations ») en plus d'appeler le service d'authentification.
    @Override
    @Transactional
    public String renvoyerInvitation(Long id) {
        exigerAfriland();
        Partenaire partenaire = chargerOuEchouer(id);
        // Le message s'adresse à une personne : on reprend le nom de
        // l'administrateur, pas la raison sociale du partenaire.
        String nomAdmin = agents.trouverParEmail(partenaire.getEmail())
                .map(Agent::getNomComplet)
                .orElse(partenaire.getNom());
        invitations.renvoyerInvitation(partenaire.getEmail(), nomAdmin, Agent.ROLE_ADMIN);
        return "Invitation renvoyée à l'administrateur de « " + partenaire.getNom() + " ».";
    }

    private void exigerAfriland() {
        if (!"SUPER_ADMIN".equals(utilisateur.roleCourant())) {
            throw new AccesReserveAfriland();
        }
    }

    /** Afriland ne doit pas pouvoir se couper elle-même l'accès à la plateforme. */
    private void exigerAutreQueLeSien(Partenaire partenaire, String action) {
        if (partenaire.getId().equals(utilisateur.partenaireIdCourant())) {
            throw new IllegalArgumentException("Vous ne pouvez pas " + action
                    + " votre propre institution : vous perdriez l'accès à la plateforme.");
        }
    }

    private Partenaire chargerOuEchouer(Long id) {
        return partenaires.trouverParId(id)
                .orElseThrow(() -> new PartenaireNonTrouve(id));
    }

    private static String obligatoire(String valeur, String message) {
        if (valeur == null || valeur.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return valeur.trim();
    }

    private static PartenaireResultat versResultat(Partenaire p) {
        String statut = p.isActif() ? "Actif" : "Désactivé";
        return new PartenaireResultat(p.getId(), p.getNom(), p.getEmail(), p.isActif(), statut);
    }
}
