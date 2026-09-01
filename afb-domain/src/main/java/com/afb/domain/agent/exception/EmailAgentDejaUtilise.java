package com.afb.domain.agent.exception;

public class EmailAgentDejaUtilise extends RuntimeException {
    public EmailAgentDejaUtilise(String email) {
        super("un compte existant avec cet email: " + email);
    }
}
