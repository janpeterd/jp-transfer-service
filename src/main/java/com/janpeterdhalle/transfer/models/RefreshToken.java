package com.janpeterdhalle.transfer.models;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@NoArgsConstructor
@Getter
@Setter
@Table(name = "REFRESH_TOKENS")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", updatable = false, nullable = false)
    private UUID id;

    @OneToOne
    private User user;

    @Column(name = "CREATION_DATE", nullable = false, updatable = false)
    private Instant creationDate;

    @Column(name = "EXPIRES_AT", nullable = false)
    private Instant expiresAt;

    @PrePersist
    protected void onCreate() {
        creationDate = Instant.now();
    }
}
