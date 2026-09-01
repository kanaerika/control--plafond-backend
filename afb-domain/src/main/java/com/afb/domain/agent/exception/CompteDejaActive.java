package com.afb.domain.agent.exception;

public class CompteDejaActive extends RuntimeException {
    public CompteDejaActive() {
        super("Ce compte est déjà activé.");
    }
}
