package com.afb.application.statistique.port.in;

import com.afb.application.statistique.dto.StatistiquesResultat;

public interface ConsulterStatistiquesUseCase {
    /** Tableau de bord adapté au rôle de l'utilisateur connecté. */
    StatistiquesResultat.Reponse pourUtilisateurCourant();
}