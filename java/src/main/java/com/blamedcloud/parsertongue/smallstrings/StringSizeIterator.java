package com.blamedcloud.parsertongue.smallstrings;

import java.util.Iterator;
import java.util.Set;

public class StringSizeIterator implements Iterator<Integer> {

    private Set<Integer> sizes;
    private int sizeCounter;
    private boolean hasOne;


    public StringSizeIterator(Set<Integer> sizes) {
        this.sizes = sizes;
        sizeCounter = 0;
        hasOne = sizes.contains(1);
    }

    @Override
    public boolean hasNext() {
        return sizes.size() > 0;
    }

    @Override
    public Integer next() {
        while (true) {
            sizeCounter++;
            if (canMakeSize(sizeCounter)) {
                return sizeCounter;
            }
        }
    }

    private boolean canMakeSize(int size) {
        if (size < 1) {
            return false;
        }

        if (hasOne) {
            return true;
        }

        for (Integer s : sizes) {
            if ((s == size) || (size % s == 0) || canMakeSize(size - s)) {
                return true;
            }
        }

        return false;
    }

}
