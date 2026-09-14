package com.afb.infrastructure.agent.adapter.out.invitation;

import com.afb.domain.agent.exception.EmailAgentDejaUtilise;
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
        try (Keycloak kc = keycloak()) {
            RealmResource realmResource = kc.realm(realm);
            UsersResource users = realmResource.users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(agent.getEmail());
            user.setEmail(agent.getEmail());
            appliquerNom(user, agent.getNomComplet());
            user.setEnabled(true);
            // L'adresse est réputée vérifiée par le lien d'invitation lui-même, et
            // aucune action obligatoire n'est posée : sans cela Keycloak réclamerait
            // à la connexion un formulaire de profil en anglais.
            user.setEmailVerified(true);
            user.setRequiredActions(List.of());

            Response response = users.create(user);
            int status = response.getStatus();
            if (status == 409) {
                throw new EmailAgentDejaUtilise(agent.getEmail());
            }
            if (status != 201) {
                log.warn("Provisioning Keycloak ignoré pour {} : HTTP {}. "
                        + "L'agent est enregistré côté application sans compte Keycloak ; "
                        + "renvoyez l'invitation une fois le client de service configuré "
                        + "(voir KEYCLOAK_SETUP.md).", agent.getEmail(), status);
                return null;
            }
            userId = extraireId(response);

            RoleRepresentation r = realmResource.roles().get(role).toRepresentation();
            users.get(userId).roles().realmLevel().add(List.of(r));
        } catch (EmailAgentDejaUtilise e) {
            throw e;
        } catch (RuntimeException e) {
            log.warn("Provisioning Keycloak indisponible pour {} ({}). "
                    + "L'agent est enregistré côté application sans compte Keycloak ; "
                    + "renvoyez l'invitation une fois le client de service configuré "
                    + "(voir KEYCLOAK_SETUP.md).", agent.getEmail(), e.getMessage());
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

    @Override
    public void renvoyerInvitation(String email, String nomComplet, String role) {
        invitations.emettre(email, nomComplet, role, true);
    }

    @Override
    public void reinitialiserMotDePasse(String email, String nomComplet, String role) {
        invitations.emettre(email, nomComplet, role, true);
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
