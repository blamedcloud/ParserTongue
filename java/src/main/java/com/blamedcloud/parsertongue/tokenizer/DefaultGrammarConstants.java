package com.blamedcloud.parsertongue.tokenizer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DefaultGrammarConstants {

    private DefaultGrammarConstants() {}

    public static final String GROUP_START = "(";
    public static final String GROUP_END = ")";

    public static final String REPEAT_START = "{";
    public static final String REPEAT_END = "}";

    public static final String OPTIONAL_START = "[";
    public static final String OPTIONAL_END = "]";

    public static final Set<String> CONTROL_START_TOKENS = new HashSet<>(Arrays.asList(GROUP_START, REPEAT_START, OPTIONAL_START));
    public static final Set<String> CONTROL_END_TOKENS = new HashSet<>(Arrays.asList(GROUP_END, REPEAT_END, OPTIONAL_END));

    public static final String ALTERNATION_SEP = "|";
    public static final String CONCATENATION_SEP = ",";

    public static final Set<String> CONTROL_SEPARATOR_TOKENS = new HashSet<>(Arrays.asList(ALTERNATION_SEP, CONCATENATION_SEP));

    public static final String COMMENT_NAME = "Comment";
    public static final String CONTROL_NAME = "Control";
    public static final String DEFINE_NAME = "Define";
    public static final String END_NAME = "End";
    public static final String EXTERNAL_NAME = "External";
    public static final String IDENTIFIER_NAME = "Identifier";
    public static final String REGEX_NAME = "Regex";
    public static final String TERMINAL_NAME = "Terminal";

    public static final String ANNOTATION_NAME = "Annotation";
    public static final String COMPOSITION_NAME = "Composition";

    public static final String EMPTY_NAME = "Empty";

}
