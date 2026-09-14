package com.afb.domain.configuration.model;

import java.time.Instant;

/** Configuration globale (plafond mensuel modifiable). Objet métier pur. */
public class Configuration {

    private final Long id;
    private long plafondMensuel;
    private String modifiePar;
    private Instant modifieLe;

    public Configuration(Long id, long plafondMensuel, String modifiePar, Instant modifieLe) {
        this.id = id;
        this.plafondMensuel = plafondMensuel;
        this.modifiePar = modifiePar;
        this.modifieLe = modifieLe;
    }

    /** Règle métier : modifier le plafond (doit être strictement positif). */
    public void modifierPlafond(long nouveauPlafond, String parQui) {
        if (nouveauPlafond <= 0) {
            throw new IllegalArgumentException("Le plafond doit être un montant positif.");
        }
        this.plafondMensuel = nouveauPlafond;
        this.modifiePar = parQui;
        this.modifieLe = Instant.now();
    }

    public Long getId() { return id; }
    public long getPlafondMensuel() { return plafondMensuel; }
    public String getModifiePar() { return modifiePar; }
    public Instant getModifieLe() { return modifieLe; }
}