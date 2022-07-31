package com.blamedcloud.parsertongue.grammar.annotations;

import com.blamedcloud.parsertongue.tokenizer.Token;

public interface AnnotationLibrary {

    public Annotation getAnnotation(Token token);

}
