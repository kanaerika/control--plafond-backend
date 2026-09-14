package com.afb.application.configuration.port.in;

import java.time.Instant;

public record ConfigurationResultat(
        long plafondMensuel,
        String modifiePar,
        Instant modifieLe) {
}