package com.blamedcloud.parsertongue.grammar.expecterator;

import java.util.Optional;

// An "Iterator" for the expect methods.
// Unlike a true Iterator, if hasNext()
// returns true, there might not actually be
// any T's left, but if hasNext() returns
// false, then there are certainly no more T's.
public interface Expecterator<T> {

    // Should return true if there might be more, and
    // false if there are certainly no more.
    public boolean hasNext();

    // try to get the next T, returning Optional.empty()
    // if none could be found.
    public Optional<T> tryNext();

    // reset the underlying tokenizer object
    public void reset();

}
