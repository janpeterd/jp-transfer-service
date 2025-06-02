package com.janpeterdhalle.transfer.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
public class Chunk {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    FileEntity file;

    @NotNull
    @PositiveOrZero
    Integer chunkIndex;

    @Size(min = 40, max = 40)
    @NotBlank
    String chunkChecksum;
}
