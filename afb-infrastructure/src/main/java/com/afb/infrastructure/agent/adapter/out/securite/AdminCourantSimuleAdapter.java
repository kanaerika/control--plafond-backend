package com.afb.infrastructure.agent.adapter.out.securite;

import com.afb.domain.agent.port.out.AdminCourantPort;
import org.springframework.stereotype.Component;


@Component
public class AdminCourantSimuleAdapter implements AdminCourantPort {

    @Override
    public Long partenaireIdCourant() {
        return 1L;   // partenaire de test — adapte à un id existant dans ta base
    }
}