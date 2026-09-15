package com.afb.infrastructure.agent.adapter.out.invitation;

import com.afb.domain.agent.exception.ServiceInvitationIndisponible;
import com.afb.domain.agent.model.Agent;
import com.afb.domain.agent.port.out.InvitationPort;
import com.afb.infrastructure.invitation.adapter.out.email.EmetteurInvitation;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

/**
 * ADAPTATEUR DE SORTIE : provisionne les comptes dans Keycloak, qui reste
 * l'unique détenteur des identifiants.
 *
 * L'email d'invitation, lui, n'est plus envoyé par Keycloak : il est composé et
 * expédié par la plateforme ({@link EmetteurInvitation}), en français et avec un
 * lien vers sa propre page d'activation. Keycloak n'a donc plus besoin d'un SMTP
 * configuré, et le compte est créé sans action obligatoire — le mot de passe est
 * posé par {@link #definirMotDePasse} à la fin du parcours d'activation.
 */
@Component
public class KeycloakInvitationAdapter implements InvitationPort {

    private static final Logger log = LoggerFactory.getLogger(KeycloakInvitationAdapter.class);

    private final String serverUrl;
    private final String realm;
    private final String clientId;
    private final String clientSecret;
    private final String roleAgent;
    private final EmetteurInvitation invitations;

    public KeycloakInvitationAdapter(
            @Value("${keycloak.admin.server-url}") String serverUrl,
            @Value("${keycloak.admin.realm}") String realm,
            @Value("${keycloak.admin.client-id}") String clientId,
            @Value("${keycloak.admin.client-secret}") String clientSecret,
            @Value("${keycloak.admin.role-agent:AGENT}") String roleAgent,
            EmetteurInvitation invitations) {
        this.serverUrl = serverUrl;
        this.realm = realm;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.roleAgent = roleAgent;
        this.invitations = invitations;
    }

    private Keycloak keycloak() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType("client_credentials")
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }

    @Override
    public String creerEtInviter(Agent agent, String role) {
        String userId;
        try {
            userId = provisionner(agent.getEmail(), agent.getNomComplet(), role);
        } catch (RuntimeException e) {
            // Pas d'email dans ce cas : le lien échouerait à l'activation faute de
            // compte. « Renvoyer l'invitation » recrée le compte puis envoie le lien.
            log.warn("Provisioning Keycloak indisponible pour {} ({}). "
                    + "L'agent est enregistré côté application sans compte Keycloak ; "
                    + "renvoyez l'invitation une fois le service rétabli.",
                    agent.getEmail(), e.getMessage());
            return null;
        }

        // Un SMTP momentanément indisponible ne doit pas faire perdre l'agent que
        // l'administrateur vient de saisir : le compte reste « Invitation en
        // attente » et le bouton « Renvoyer l'invitation » relance l'envoi, qui
        // lui signale l'échec sans détour. L'erreur est tracée, jamais avalée en
        // silence comme c'était le cas auparavant.
        try {
            invitations.emettre(agent.getEmail(), agent.getNomComplet(), role, false);
        } catch (RuntimeException e) {
            log.error("Compte créé pour {} mais l'email d'invitation n'est pas parti : {}. "
                    + "Utilisez « Renvoyer l'invitation » une fois le SMTP rétabli.",
                    agent.getEmail(), e.getMessage(), e);
        }
        return userId;
    }

    /**
     * Garantit le compte avant d'envoyer le lien : c'est précisément le bouton
     * qui sert à rattraper une création faite pendant une panne de Keycloak.
     */
    @Override
    public void renvoyerInvitation(String email, String nomComplet, String role) {
        provisionnerOuSignaler(email, nomComplet, role);
        invitations.emettre(email, nomComplet, role, true);
    }

    @Override
    public void reinitialiserMotDePasse(String email, String nomComplet, String role) {
        provisionnerOuSignaler(email, nomComplet, role);
        invitations.emettre(email, nomComplet, role, true);
    }

    @Override
    public void supprimerCompte(String email) {
        Runnable suppression = () -> {
            try (Keycloak kc = keycloak()) {
                UsersResource users = kc.realm(realm).users();
                users.searchByEmail(email, true).forEach(u -> users.delete(u.getId()));
                log.info("Compte d'authentification supprimé pour {}", email);
            } catch (RuntimeException e) {
                // La suppression métier est déjà validée : on trace sans la défaire.
                log.error("Compte Keycloak de {} non supprimé ({}) : l'adresse restera "
                        + "réservée tant qu'il n'est pas retiré manuellement.",
                        email, e.getMessage(), e);
            }
        };
        // Après validation seulement : si la transaction échoue (agent avec
        // historique, par exemple), l'agent existe toujours et doit garder son accès.
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    suppression.run();
                }
            });
        } else {
            suppression.run();
        }
    }

    /**
     * Crée le compte, ou reprend celui qui existe déjà pour cet email. L'appelant a
     * vérifié qu'aucun agent applicatif ne porte l'adresse : un compte Keycloak
     * existant est donc un reliquat (agent supprimé avant que la suppression ne se
     * propage à Keycloak), qu'on remet en état au lieu de bloquer l'adresse à vie.
     */
    private String provisionner(String email, String nomComplet, String role) {
        try (Keycloak kc = keycloak()) {
            RealmResource realmResource = kc.realm(realm);
            UsersResource users = realmResource.users();

            UserRepresentation user = users.searchByEmail(email, true).stream().findFirst()
                    .orElse(null);
            String userId;
            if (user == null) {
                user = new UserRepresentation();
                user.setUsername(email);
                user.setEmail(email);
                preparer(user, nomComplet);
                Response response = users.create(user);
                if (response.getStatus() != 201) {
                    throw new IllegalStateException("création refusée (HTTP " + response.getStatus() + ")");
                }
                userId = extraireId(response);
            } else {
                userId = user.getId();
                log.warn("Compte Keycloak existant repris pour {}", email);
                preparer(user, nomComplet);
                users.get(userId).update(user);
            }

            RoleRepresentation r = realmResource.roles().get(role).toRepresentation();
            users.get(userId).roles().realmLevel().add(List.of(r));
            return userId;
        }
    }

    private void provisionnerOuSignaler(String email, String nomComplet, String role) {
        try {
            provisionner(email, nomComplet, role);
        } catch (RuntimeException e) {
            log.error("Provisioning Keycloak impossible pour {}", email, e);
            throw new ServiceInvitationIndisponible(
                    "Le service d'authentification (Keycloak) est momentanément indisponible "
                    + "ou n'est pas encore configuré. Réessayez plus tard ou contactez votre "
                    + "administrateur technique.", e);
        }
    }

    /**
     * Compte actif, adresse réputée vérifiée (le lien d'invitation en fait foi) et
     * aucune action obligatoire : sans cela Keycloak réclamerait à la connexion un
     * formulaire de profil en anglais.
     */
    private static void preparer(UserRepresentation user, String nomComplet) {
        appliquerNom(user, nomComplet);
        user.setEnabled(true);
        user.setEmailVerified(true);
        user.setRequiredActions(List.of());
    }

    @Override
    public void definirMotDePasse(String email, String motDePasse) {
        try (Keycloak kc = keycloak()) {
            UsersResource users = kc.realm(realm).users();
            String userId = idParEmail(users, email);

            CredentialRepresentation mdp = new CredentialRepresentation();
            mdp.setType(CredentialRepresentation.PASSWORD);
            mdp.setValue(motDePasse);
            mdp.setTemporary(false);
            users.get(userId).resetPassword(mdp);

            // Le compte doit être immédiatement utilisable : plus aucune action en attente.
            UserRepresentation user = users.get(userId).toRepresentation();
            user.setRequiredActions(List.of());
            user.setEmailVerified(true);
            users.get(userId).update(user);
        } catch (ServiceInvitationIndisponible e) {
            throw e;
        } catch (RuntimeException e) {
            log.error("Impossible de poser le mot de passe pour {}", email, e);
            throw new ServiceInvitationIndisponible(
                    "Le service d'authentification est momentanément indisponible. "
                    + "Réessayez dans quelques instants.", e);
        }
    }

    private static String idParEmail(UsersResource users, String email) {
        return users.searchByEmail(email, true).stream().findFirst()
                .map(UserRepresentation::getId)
                .orElseThrow(() -> new ServiceInvitationIndisponible(
                        "Aucun compte n'existe encore dans le service d'authentification pour "
                        + email + ". Demandez une nouvelle invitation à votre administrateur."));
    }

    /**
     * L'application ne connaît qu'un « nom complet » ; Keycloak exige prénom ET nom
     * sous peine d'imposer un formulaire de profil à la première connexion. On coupe
     * au premier espace, et à défaut on répète le seul mot disponible.
     */
    private static void appliquerNom(UserRepresentation user, String nomComplet) {
        String nom = nomComplet == null ? "" : nomComplet.trim();
        int espace = nom.indexOf(' ');
        if (espace > 0) {
            user.setFirstName(nom.substring(0, espace));
            user.setLastName(nom.substring(espace + 1).trim());
        } else {
            user.setFirstName(nom);
            user.setLastName(nom);
        }
    }

    private static String extraireId(Response response) {
        String location = response.getHeaderString("Location");
        return location.substring(location.lastIndexOf('/') + 1);
    }
}
