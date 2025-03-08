package com.janpeterdhalle.transfer.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "shared_links")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@ToString
public class SharedLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Lob
    @Column(nullable = false)
    private String url;

    @Lob
    @Column(nullable = false)
    private String downloadLink;

    @Lob
    @Column(nullable = false)
    private String ownerMailBase64;

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

    @Column(nullable = false)
    @Builder.Default
    private Long fileSize = 0L;

    private String fileName;

    @Column(nullable = false)
    @Builder.Default
    private boolean isProtected = false;

    @Lob
    private String password;
}
