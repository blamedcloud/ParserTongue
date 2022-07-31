package com.blamedcloud.parsertongue.grammar.annotations;

import static com.blamedcloud.parsertongue.tokenizer.DefaultGrammarConstants.COMPOSITION_NAME;

import java.util.List;

import com.blamedcloud.parsertongue.grammar.Rule;
import com.blamedcloud.parsertongue.grammar.result.ParseResultFunction;
import com.blamedcloud.parsertongue.grammar.result.ParseResultTransformer;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class AnnotationManager {

    AnnotationLibrary annotations;

    public static AnnotationManager getDefaultManager() {
        return new AnnotationManager(new DefaultAnnotationLibrary());
    }

    public AnnotationManager(AnnotationLibrary library) {
        annotations = library;
    }

    public ParseResultFunction parseAnnotation(Tokenizer tokens) {
        Token compositionToken = new Token(".", tokens.getTTL().get(COMPOSITION_NAME));

        if (tokens.getLastToken().isSameAs(compositionToken)) {
            throw new RuntimeException("Missing annotation after composition token");
        }

        List<Tokenizer> annotationTokenizers = tokens.splitTokensOn(compositionToken);

        ParseResultFunction function = ParseResultTransformer.identity;
        for (int i = annotationTokenizers.size() - 1; i >= 0 ; i--) {
            Tokenizer annotationTokens = annotationTokenizers.get(i);
            Token primaryToken = annotationTokens.getFirstToken();
            Annotation annotation = annotations.getAnnotation(primaryToken);
            ParseResultFunction annotationFunction = annotation.getFunction(annotationTokens);
            function = Rule.compose(annotationFunction, function);
        }

        return function;
    }

}
