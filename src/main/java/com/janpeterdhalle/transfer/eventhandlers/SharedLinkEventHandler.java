package com.janpeterdhalle.transfer.eventhandlers;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.models.SharedLink;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.core.annotation.HandleBeforeDelete;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@RepositoryEventHandler
public class SharedLinkEventHandler {
    @HandleBeforeDelete
    public void handleBeforeDelete(SharedLink sharedLink) throws IOException {
        // Delete the associated folder
        Utils.deleteDirectory(new File(Paths.get("uploads/" + sharedLink.getOwnerMailBase64() + "/" + sharedLink.getFileName())
                                            .toString()));
        log.info("Deleted shared link data path: " + sharedLink.getOwnerMailBase64() + "/" + sharedLink.getFileName());
    }
}
