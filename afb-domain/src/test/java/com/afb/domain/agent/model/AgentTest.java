package com.afb.domain.agent.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgentTest {

    @Test
    void nouveauCompteActifEnAttenteDInvitation() {
        Agent a = Agent.creer("Paul Ngassa", "  Paul.Ngassa@Example.local ", 9L, "Douala", "TST042");

        assertNull(a.getId());
        assertEquals("paul.ngassa@example.local", a.getEmail());
        assertEquals(Agent.ROLE_AGENT, a.getRole());
        assertTrue(a.estAgent());
        assertTrue(a.isActif());
        assertFalse(a.isInvitationAcceptee());
        assertTrue(a.isFirstLogin());
        assertEquals("Invitation en attente", a.statut());
    }

    @Test
    void roleAbsentRetombeSurAgent() {
        Agent a = Agent.builder().nomComplet("X").email("x@y.z").role(" ").build();
        assertEquals(Agent.ROLE_AGENT, a.getRole());
    }

    @Test
    void nomEtEmailObligatoires() {
        assertThrows(IllegalArgumentException.class, () -> Agent.builder().email("x@y.z").build());
        assertThrows(IllegalArgumentException.class, () -> Agent.builder().nomComplet("X").build());
    }

    /** Chemin du rechargement depuis la base : aucun drapeau ne doit être perdu. */
    @Test
    void copieConserveTousLesChampsSaufCeuxModifies() {
        Agent origine = Agent.builder()
                .id(16L).nomComplet("Agent Test").email("agent.test@afriland.local")
                .role(Agent.ROLE_ADMIN).partenaireId(9L).agence("Agence Centrale").codeAgent("C1")
                .actif(false).invitationAcceptee(true).firstLogin(false)
                .build();

        Agent maj = origine.copie().nomComplet("Agent Renommé").agence("Yaoundé").build();

        assertEquals(16L, maj.getId());
        assertEquals("Agent Renommé", maj.getNomComplet());
        assertEquals("Yaoundé", maj.getAgence());
        assertEquals("agent.test@afriland.local", maj.getEmail());
        assertEquals(Agent.ROLE_ADMIN, maj.getRole());
        assertEquals(9L, maj.getPartenaireId());
        assertEquals("C1", maj.getCodeAgent());
        assertFalse(maj.isActif());
        assertTrue(maj.isInvitationAcceptee());
        assertFalse(maj.isFirstLogin());
        assertEquals("Actif", maj.statut());
        assertEquals(origine, maj);
    }
}
