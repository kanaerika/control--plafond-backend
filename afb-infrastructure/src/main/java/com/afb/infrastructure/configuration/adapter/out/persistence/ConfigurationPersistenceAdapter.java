package com.afb.infrastructure.configuration.adapter.out.persistence;

import com.afb.domain.configuration.model.Configuration;
import com.afb.domain.configuration.port.out.ConfigurationRepositoryPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ConfigurationPersistenceAdapter implements ConfigurationRepositoryPort {

    private final ConfigurationJpaRepository jpa;

    public ConfigurationPersistenceAdapter(ConfigurationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Configuration> lire() {
        return jpa.findById(1L).map(ConfigurationPersistenceAdapter::versDomaine);
    }

    @Override
    public Configuration enregistrer(Configuration c) {
        ConfigurationJpaEntity e = jpa.findById(c.getId()).orElseGet(ConfigurationJpaEntity::new);
        e.setId(c.getId());
        e.setPlafondMensuel(c.getPlafondMensuel());
        e.setModifiePar(c.getModifiePar());
        e.setModifieLe(c.getModifieLe());
        return versDomaine(jpa.save(e));
    }

    private static Configuration versDomaine(ConfigurationJpaEntity e) {
        return new Configuration(e.getId(), e.getPlafondMensuel(),
                e.getModifiePar(), e.getModifieLe());
    }
}