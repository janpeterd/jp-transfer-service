package com.janpeterdhalle.transfer.repositories;

import com.janpeterdhalle.transfer.models.SharedLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource(collectionResourceRel = "sharedLinks", path = "sharedLinks")
public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {
    List<SharedLink> findSharedLinksByOwnerMailBase64(String encodedEmail);
    Optional<SharedLink> findSharedLinksByFileNameAndOwnerMailBase64(String fileName, String encodedEmail);
}
