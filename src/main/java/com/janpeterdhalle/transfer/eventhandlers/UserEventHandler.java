package com.janpeterdhalle.transfer.eventhandlers;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.models.User;
import com.janpeterdhalle.transfer.repositories.SharedLinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

@RepositoryEventHandler
@Slf4j
@RequiredArgsConstructor
public class UserEventHandler {
    private final SharedLinkRepository sharedLinkRepository;

    @HandleBeforeDelete
    public void handleBeforeDelete(User user) throws IOException {
        // find all associated links
        String encodedEmail = Base64.getEncoder().encodeToString(user.getEmail().getBytes());
        List<SharedLink> sharedLinks = sharedLinkRepository.findSharedLinksByOwnerMailBase64(encodedEmail);

        log.info("Deleting all data associated to user, before deleting user");
        for (SharedLink sharedLink : sharedLinks) {
            // Delete the associated folder
            Utils.deleteDirectory(new File(Paths.get("uploads/" + sharedLink.getOwnerMailBase64() + "/" + sharedLink.getFileName())
                                                .toString()));
            log.info("Deleted shared link data path: " + sharedLink.getOwnerMailBase64() + "/" + sharedLink.getFileName());
        }
    }
}
