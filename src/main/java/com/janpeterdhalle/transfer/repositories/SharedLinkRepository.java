package com.janpeterdhalle.transfer.repositories;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.models.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SharedLinkRepository extends JpaRepository<SharedLink, Long> {
    Optional<SharedLink> findByTransfer(Transfer transfer);

    Optional<SharedLink> findSharedLinkByUuid(String uuid);
}
