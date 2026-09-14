package com.afb.infrastructure.transfert.adapter.out.config;

import com.afb.application.configuration.port.in.GererConfigurationUseCase;
import com.afb.domain.transfert.port.out.PlafondPort;
import org.springframework.stereotype.Component;

/**
 * Fournit le plafond mensuel à la vérification, en le lisant depuis la
 * configuration modifiable (cache mémoire), et non plus une valeur fixe.
 */
@Component
public class PlafondAdapter implements PlafondPort {

    private final GererConfigurationUseCase configuration;

    public PlafondAdapter(GererConfigurationUseCase configuration) {
        this.configuration = configuration;
    }

    @Override
    public long plafondMensuel() {
        return configuration.plafondMensuel();
    }
}