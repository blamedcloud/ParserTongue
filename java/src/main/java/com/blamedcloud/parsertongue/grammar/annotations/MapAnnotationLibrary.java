package com.blamedcloud.parsertongue.grammar.annotations;

import java.util.HashMap;
import java.util.Map;

import com.blamedcloud.parsertongue.tokenizer.Token;

public class MapAnnotationLibrary implements AnnotationLibrary {

    private Map<String, Annotation> annotationMap;

    public MapAnnotationLibrary() {
        annotationMap = new HashMap<>();
    }

    public void addAnnotation(String key, Annotation value) {
        annotationMap.put(key, value);
    }

    @Override
    public Annotation getAnnotation(Token token) {
        String key = token.getValue();
        if (annotationMap.containsKey(key)) {
            return annotationMap.get(key);
        } else {
            throw new RuntimeException("Annotation not found");
        }
    }

}
