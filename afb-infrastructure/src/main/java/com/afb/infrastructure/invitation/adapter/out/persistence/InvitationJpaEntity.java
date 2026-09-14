package com.afb.infrastructure.invitation.adapter.out.persistence;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "invitations",
       indexes = {
           @Index(name = "idx_invitations_empreinte", columnList = "empreinte_token", unique = true),
           @Index(name = "idx_invitations_email", columnList = "email")
       })
public class InvitationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** SHA-256 du jeton : le jeton en clair n'est jamais persisté. */
    @Column(name = "empreinte_token", nullable = false, unique = true, length = 64)
    private String empreinteToken;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Instant expiration;

    @Column(name = "utilisee_le")
    private Instant utiliseeLe;

    protected InvitationJpaEntity() {}

    public Long getId() { return id; }
    public void setId(Long v) { this.id = v; }
    public String getEmpreinteToken() { return empreinteToken; }
    public void setEmpreinteToken(String v) { this.empreinteToken = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { this.email = v; }
    public Instant getExpiration() { return expiration; }
    public void setExpiration(Instant v) { this.expiration = v; }
    public Instant getUtiliseeLe() { return utiliseeLe; }
    public void setUtiliseeLe(Instant v) { this.utiliseeLe = v; }
}
