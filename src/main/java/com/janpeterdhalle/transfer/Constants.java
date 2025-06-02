package com.janpeterdhalle.transfer;

import java.util.Set;

public class Constants {
    private void Constants() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    public final static Set<String> forbiddenDeleteDirs = Set.of("/", "~");
}
