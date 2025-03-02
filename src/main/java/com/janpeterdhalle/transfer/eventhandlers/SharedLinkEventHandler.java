package com.janpeterdhalle.transfer.eventhandlers;

import com.janpeterdhalle.transfer.models.SharedLink;
import com.janpeterdhalle.transfer.services.SchedulingService;
import com.janpeterdhalle.transfer.services.SharedLinkService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import java.io.IOException;

@Slf4j
@RepositoryEventHandler
@RequiredArgsConstructor
public class SharedLinkEventHandler {
    private final SchedulingService schedulingService;

    @HandleBeforeDelete
    public void handleBeforeDelete(SharedLink sharedLink) throws IOException {
        // Delete the associated folder
        SharedLinkService.deletedAssociatedData(sharedLink);
        log.info("Deleted shared link data path: " + sharedLink.getOwnerMailBase64() + "/" + sharedLink.getFileName());
    }
}
