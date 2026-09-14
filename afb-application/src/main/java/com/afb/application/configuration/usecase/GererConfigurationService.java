package com.afb.application.configuration.usecase;

import com.afb.application.configuration.port.in.ConfigurationResultat;
import com.afb.application.configuration.port.in.GererConfigurationUseCase;
import com.afb.domain.configuration.model.Configuration;
import com.afb.domain.configuration.port.out.ConfigurationRepositoryPort;
import com.afb.domain.statistique.port.out.UtilisateurCourantPort;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Gère la configuration globale (plafond mensuel). Fidèle à l'ancien backend :
 *  - ligne unique (id=1) créée au démarrage depuis app.plafond-mensuel
 *  - plafond en cache mémoire (lu à chaque contrôle de transfert)
 *  - modification réservée à l'admin Afriland (SUPER_ADMIN), montant positif
 */
@Service
public class GererConfigurationService implements GererConfigurationUseCase, InitializingBean {

    private static final Long ID = 1L;

    private final ConfigurationRepositoryPort repo;
    private final UtilisateurCourantPort utilisateur;
    private final long plafondAmorcage;

    private volatile long plafondCache;

    public GererConfigurationService(ConfigurationRepositoryPort repo,
                                     UtilisateurCourantPort utilisateur,
                                     @Value("${app.plafond-mensuel:1000000}") long plafondAmorcage) {
        this.repo = repo;
        this.utilisateur = utilisateur;
        this.plafondAmorcage = plafondAmorcage;
    }

    @Override
    @Transactional
    public void afterPropertiesSet() {
        Configuration config = repo.lire().orElseGet(() -> {
            Configuration c = new Configuration(ID, plafondAmorcage, "système", Instant.now());
            return repo.enregistrer(c);
        });
        this.plafondCache = config.getPlafondMensuel();
    }

    @Override
    public long plafondMensuel() {
        return plafondCache;
    }

    @Override
    @Transactional(readOnly = true)
    public ConfigurationResultat lire() {
        Configuration c = repo.lire().orElseThrow(() ->
                new IllegalStateException("Configuration non initialisée."));
        return versResultat(c);
    }

    @Override
    @Transactional
    public ConfigurationResultat modifierPlafond(long nouveauPlafond) {
        // Réservé à l'admin Afriland (SUPER_ADMIN)
        if (!"SUPER_ADMIN".equals(utilisateur.roleCourant())) {
            throw new SecurityException("Réservé à l'administrateur Afriland.");
        }
        Configuration c = repo.lire().orElseThrow(() ->
                new IllegalStateException("Configuration non initialisée."));

        c.modifierPlafond(nouveauPlafond, "admin");  // le nom réel viendra du JWT plus tard
        Configuration sauve = repo.enregistrer(c);
        this.plafondCache = sauve.getPlafondMensuel();  // rafraîchit le cache
        return versResultat(sauve);
    }

    private static ConfigurationResultat versResultat(Configuration c) {
        return new ConfigurationResultat(c.getPlafondMensuel(), c.getModifiePar(), c.getModifieLe());
    }
}