package com.blamedcloud.parsertongue.grammar;

public class WalkResult {

    public final boolean isInfinite;
    public final int treeSize;

    WalkResult(boolean infinite, int size) {
        isInfinite = infinite;
        treeSize = size;
    }

}
