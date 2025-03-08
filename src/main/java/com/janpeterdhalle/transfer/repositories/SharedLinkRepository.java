package com.janpeterdhalle.transfer.repositories;

import com.janpeterdhalle.transfer.models.SharedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "sharedLinks", path = "sharedLinks")
public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {
    List<SharedLink> findSharedLinksByOwnerMailBase64(String encodedEmail);

    Optional<SharedLink> findSharedLinksByFileNameAndOwnerMailBase64(String fileName, String encodedEmail);
    List<SharedLink> findAllByFileNameAndOwnerMailBase64(String fileName, String encodedEmail);

    List<SharedLink> findAllByExpiresAtAfter(LocalDateTime expiresAt);
    // Update this method to use findOne pattern instead of findSharedLinks...
    @Query("SELECT s FROM SharedLink s WHERE s.fileName = :fileName AND s.ownerMailBase64 = :ownerMailBase64 ORDER BY s.id ASC LIMIT 1")
    Optional<SharedLink> findOneByFileNameAndOwnerMailBase64(
            @Param("fileName") String fileName,
            @Param("ownerMailBase64") String ownerMailBase64);

}
