package com.janpeterdhalle.transfer.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    @Builder.Default
    boolean active = true;

    LocalDateTime startTime;
    LocalDateTime endTime;

    @NotBlank
    String uploadPath;

    @ManyToOne
    User user;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn
    SharedLink sharedLink;

    @OneToMany(mappedBy = "transfer", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    Set<FileEntity> files = new HashSet<>();
}
