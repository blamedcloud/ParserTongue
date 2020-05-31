package com.blamedcloud.parsertongue.smallstrings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SmallestStringIterator implements Iterator<String> {

    private Set<Integer> sizes;
    private Map<Integer, Set<String>> lettersOfSize;

    private int currentSize;
    private List<String> stringsOfCurrentSize;
    private int currentSizeIndex;

    StringSizeIterator sizeIterator;

    public SmallestStringIterator(Set<String> alphabet) {
        sizes = new HashSet<>();
        lettersOfSize = new HashMap<>();
        for (String letter : alphabet) {
            if (letter.length() > 0) {
                Integer size = letter.length();
                sizes.add(size);
                if (lettersOfSize.containsKey(size)) {
                    lettersOfSize.get(size).add(letter);
                } else {
                    Set<String> letters = new HashSet<>();
                    letters.add(letter);
                    lettersOfSize.put(size, letters);
                }
            }
        }
        sizeIterator = new StringSizeIterator(sizes);
        if (sizeIterator.hasNext()) {
            currentSize = sizeIterator.next();
            stringsOfCurrentSize = stringsOfSize(currentSize);
            currentSizeIndex = 0;
        } else {
            currentSize = 0;
        }
    }

    private List<String> stringsOfSize(int size) {
        Set<String> strings = new HashSet<>();
        PartitionsIterator partitionsIterator = new PartitionsIterator(size, sizes);
        while (partitionsIterator.hasNext()) {
            List<Integer> partition = partitionsIterator.next();
            if (partition.size() > 0) {
                strings.addAll(stringsInPartition(partition, 0));
            }
        }
        List<String> stringList = new ArrayList<>();
        stringList.addAll(strings);
        Collections.sort(stringList);

        return stringList;
    }

    private Set<String> stringsInPartition(List<Integer> partition, int index) {
        Set<String> strings = new HashSet<>();
        if (partition.size() == 0) {
            return strings;
        }
        if (!lettersOfSize.containsKey(partition.get(index))) {
            return strings;
        }

        Set<String> prefixes = lettersOfSize.get(partition.get(index));
        if (index < partition.size() - 1) {
            Set<String> suffixes = stringsInPartition(partition, index + 1);
            for (String prefix : prefixes) {
                for (String suffix : suffixes) {
                    strings.add(prefix + suffix);
                }
            }
        } else {
            strings = prefixes;
        }
        return strings;
    }

    @Override
    public boolean hasNext() {
        return sizeIterator.hasNext();
    }

    @Override
    public String next() {
        if (currentSizeIndex < stringsOfCurrentSize.size()) {
            String value = stringsOfCurrentSize.get(currentSizeIndex);
            currentSizeIndex++;
            return value;
        } else {
            currentSize = sizeIterator.next();
            stringsOfCurrentSize = stringsOfSize(currentSize);
            currentSizeIndex = 0;
            return next();
        }
    }

}
