package com.blamedcloud.parsertongue.utility;

import java.util.Objects;

public class FixedPair<L,R> {

    public final L left;
    public final R right;

    public FixedPair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public FixedPair(Pair<L,R> pair) {
        this.left = pair.left;
        this.right = pair.right;
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        if (!(o instanceof FixedPair)) {
            return false;
        }
        FixedPair<?, ?> other = (FixedPair<?, ?>) o;

        return Objects.equals(left, other.left) && Objects.equals(right, other.right);
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

}
