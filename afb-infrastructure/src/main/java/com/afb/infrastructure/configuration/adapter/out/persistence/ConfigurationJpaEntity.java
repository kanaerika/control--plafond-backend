package com.afb.infrastructure.configuration.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "configuration")
public class ConfigurationJpaEntity {

    @Id
    private Long id = 1L;

    @Column(nullable = false)
    private long plafondMensuel;

    private String modifiePar;
    private Instant modifieLe;

    public ConfigurationJpaEntity() {
        // Requis par JPA : Hibernate instancie l'entité par réflexion, puis hydrate ses champs.
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public long getPlafondMensuel() { return plafondMensuel; }
    public void setPlafondMensuel(long v) { this.plafondMensuel = v; }
    public String getModifiePar() { return modifiePar; }
    public void setModifiePar(String v) { this.modifiePar = v; }
    public Instant getModifieLe() { return modifieLe; }
    public void setModifieLe(Instant v) { this.modifieLe = v; }
}