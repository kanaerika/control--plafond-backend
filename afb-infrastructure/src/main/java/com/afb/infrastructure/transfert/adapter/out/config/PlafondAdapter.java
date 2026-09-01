package com.afb.infrastructure.transfert.adapter.out.config;

import com.afb.domain.transfert.port.out.PlafondPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PlafondAdapter implements PlafondPort {

    private final long plafond;

    public PlafondAdapter(@Value("${app.plafond-mensuel:1000000}") long plafond) {
        this.plafond = plafond;
    }

    @Override
    public long plafondMensuel() {
        return plafond;
    }
}