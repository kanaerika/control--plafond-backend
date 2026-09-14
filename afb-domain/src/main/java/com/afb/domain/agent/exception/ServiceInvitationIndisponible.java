package com.afb.domain.agent.exception;

/**
 * Levée quand l'envoi (ou le renvoi) d'une invitation ne peut pas aboutir parce
 * que le service d'authentification Keycloak est injoignable ou pas encore
 * configuré pour cette opération (client de service absent / secret non
 * renseigné). Porte un message compréhensible par un administrateur métier,
 * pas une trace technique jakarta.ws.rs.*.
 */
public class ServiceInvitationIndisponible extends RuntimeException {

    public ServiceInvitationIndisponible(String message) {
        super(message);
    }

    public ServiceInvitationIndisponible(String message, Throwable cause) {
        super(message, cause);
    }
}
