package com.janpeterdhalle.transfer.models;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "shared_links")
@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@ToString
public class SharedLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @NotBlank
    @Column(nullable = false, updatable = false)
    @Builder.Default
    String uuid = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String downloadLink;

    @ManyToOne
    User user;

    @OneToOne(mappedBy = "sharedLink")
    Transfer transfer;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private Long createdAt = System.currentTimeMillis();

    @UpdateTimestamp
    @Column(nullable = false)
    @Builder.Default
    private Long updatedAt = System.currentTimeMillis();

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Integer downloads = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer maxDownloads = 100;

}
