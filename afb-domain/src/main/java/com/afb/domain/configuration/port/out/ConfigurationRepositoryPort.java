package com.afb.domain.configuration.port.out;

import com.afb.domain.configuration.model.Configuration;
import java.util.Optional;

public interface ConfigurationRepositoryPort {
    Optional<Configuration> lire();
    Configuration enregistrer(Configuration configuration);
}