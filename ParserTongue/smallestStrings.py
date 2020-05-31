#!/usr/bin/env python3
#smallestStrings.py

def smallestStrGen(alphabet, sort = False):
    alphabet = sorted(alphabet, key=lambda x: len(x))
    sizes = set([len(x) for x in alphabet])
    lettersOfSize = {size: [letter for letter in alphabet if len(letter) == size] for size in sizes}

    def sizeGen():
        size = 1
        while True:
            if canMakeSize(size, sizes):
                yield size
            size += 1

    def stringsOfSize(size, _sort):
        partitions = partitionsGen(size, sizes)
        strings = []
        for part in partitions:
            strings += stringsInPartition(part)
        uniqueStrings = set(strings)
        if _sort:
            uniqueStrings = sorted(list(uniqueStrings))
        for s in uniqueStrings:
            yield s

    def stringsInPartition(partition):
        strings = []
        beginSet = lettersOfSize[partition[0]]
        if len(partition) > 1:
            rest = stringsInPartition(partition[1:])
            for s1 in beginSet:
                for s2 in rest:
                    strings.append(s1+s2)
        else:
            strings = beginSet
        return strings

    def nextStr():
        for size in sizeGen():
            for string in stringsOfSize(size, sort):
                yield string

    return nextStr

def partitionsGen(size, sizes):
    if size < 1:
        return
    for s in sizes:
        if s == size:
            yield [s]
        else:
            for p in partitionsGen(size - s, sizes):
                yield [s] + p

def canMakeSize(size, sizes):
    if size < 1:
        return False

    if 1 in sizes:
        return True

    for s in sizes:
        if s == size or (size % s == 0) or canMakeSize(size - s, sizes):
            return True
    return False


