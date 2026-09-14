package com.afb.application.configuration.port.in;

public interface GererConfigurationUseCase {

    /** Lecture rapide du plafond mensuel courant (utilisée par la vérification). */
    long plafondMensuel();

    /** Détail de la configuration (plafond + traçabilité). */
    ConfigurationResultat lire();

    /** Modifie le plafond (réservé à l'admin Afriland). */
    ConfigurationResultat modifierPlafond(long nouveauPlafond);
}