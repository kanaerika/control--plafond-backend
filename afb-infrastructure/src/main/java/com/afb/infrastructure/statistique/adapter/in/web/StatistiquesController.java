package com.afb.infrastructure.statistique.adapter.in.web;

import com.afb.application.statistique.dto.StatistiquesResultat;
import com.afb.application.statistique.port.in.ConsulterStatistiquesUseCase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/statistiques")
@PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
public class StatistiquesController {

    private final ConsulterStatistiquesUseCase consulterStatistiques;

    public StatistiquesController(ConsulterStatistiquesUseCase consulterStatistiques) {
        this.consulterStatistiques = consulterStatistiques;
    }

    @GetMapping
    public StatistiquesResultat.Reponse statistiques() {
        return consulterStatistiques.pourUtilisateurCourant();
    }
}