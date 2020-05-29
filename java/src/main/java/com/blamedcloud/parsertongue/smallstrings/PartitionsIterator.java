package com.blamedcloud.parsertongue.smallstrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class PartitionsIterator implements Iterator<List<Integer>> {

    private int partitionSize;
    private boolean hasExactSize;
    private List<Integer> smallerSizes;
    private Set<Integer> smallerSizesSet;
    private int index;

    private boolean firstIteration;

    private PartitionsIterator childIterator;

    public PartitionsIterator(int partitionSize, Set<Integer> sizes) {
        this.partitionSize = partitionSize;
        hasExactSize = false;
        smallerSizes = new ArrayList<>();
        smallerSizesSet = new HashSet<>();
        index = 0;
        childIterator = null;
        firstIteration = true;

        if (partitionSize >= 1) {
            for (Integer size : sizes) {
                if (size == partitionSize) {
                    hasExactSize = true;
                } else if (size < partitionSize) {
                    smallerSizes.add(size);
                    smallerSizesSet.add(size);
                }
            }
        }
        Collections.sort(smallerSizes);
    }

    @Override
    public boolean hasNext() {
        if (partitionSize < 1) {
            return false;
        }
        if (index < smallerSizes.size() - 1) {
            return true;
        }
        if (hasExactSize || firstIteration) {
            return true;
        }
        if (childIterator != null) {
            return childIterator.hasNext();
        }
        return false;
    }

    @Override
    public List<Integer> next() {
        List<Integer> partition = new ArrayList<>();

        firstIteration = false;

        if (index < smallerSizes.size()) {
            Integer thisSize = smallerSizes.get(index);
            if (childIterator == null) {
                childIterator = new PartitionsIterator(partitionSize - thisSize, smallerSizesSet);
            }
            if (childIterator.hasNext()) {
                List<Integer> remaining = childIterator.next();
                // if it returned an empty list, it didn't really have a next()
                if (remaining.size() > 0) {
                    partition.add(thisSize);
                    partition.addAll(remaining);
                }
            } else {
                index++;
                childIterator = null;
                return next();
            }
        } else if (hasExactSize) {
            hasExactSize = false;
            partition.add(partitionSize);
        }

        return partition;
    }

}
