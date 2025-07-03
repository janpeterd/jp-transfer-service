package com.janpeterdhalle.transfer.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.models.Transfer;
import com.janpeterdhalle.transfer.models.User;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findAllByUserAndActiveTrue(User user);

    Optional<Transfer> findBySharedLinkAndActiveTrue(SharedLink sharedLink);
}
