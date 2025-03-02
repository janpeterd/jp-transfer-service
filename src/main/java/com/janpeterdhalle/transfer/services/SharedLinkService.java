package com.janpeterdhalle.transfer.services;

import com.janpeterdhalle.transfer.Utils;
import com.janpeterdhalle.transfer.models.SharedLink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SharedLinkService {
    public static void deletedAssociatedData(SharedLink sharedLink) throws IOException {
        Path dataDir = Paths.get("uploads/" + sharedLink.getOwnerMailBase64() + "/" + sharedLink.getFileName());
        Utils.deleteDirectory(new File(dataDir.toString()));
    }
}
