package com.janpeterdhalle.transfer.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.models.Transfer;

public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {
    Optional<SharedLink> findByTransfer(Transfer transfer);

    Optional<SharedLink> findSharedLinkByUuid(String uuid);
}
