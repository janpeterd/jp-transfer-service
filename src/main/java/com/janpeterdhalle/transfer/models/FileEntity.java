package com.janpeterdhalle.transfer.models;

import java.util.Set;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank
    String fileName;
    String fileType;
    @NotNull
    @Positive
    Long fileSize;

    @NotNull
    @Positive
    Integer totalChunks;

    // sha1
    @NotBlank
    @Column(updatable = false, length = 40)
    String fileChecksum;

    @NotNull(message = "A file must be part of a transfer")
    @ManyToOne
    @JoinColumn
    Transfer transfer;

    @ManyToOne
    User user;

    @NotNull
    @Positive
    Integer chunkSize;

    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    Set<Chunk> chunks;

    @Builder.Default
    Boolean uploaded = false;
}
