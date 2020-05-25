package com.blamedcloud.parsertongue.grammar;

public enum RHSType {

    IDENTIFIER(RHSKind.LEAF),
    TERMINAL(RHSKind.LEAF),
    REGEX(RHSKind.LEAF),
    ALTERNATION(RHSKind.LIST),
    CONCATENATION(RHSKind.LIST),
    OPTIONAL(RHSKind.SINGLE),
    REPEAT(RHSKind.SINGLE),
    GROUP(RHSKind.SINGLE);

    private final RHSKind kind;

    public RHSKind getKind() {
        return kind;
    }

    RHSType(RHSKind kind) {
        this.kind = kind;
    }

}
