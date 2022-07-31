package com.blamedcloud.parsertongue.grammar.dependencies;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blamedcloud.parsertongue.grammar.Grammar;
import com.blamedcloud.parsertongue.grammar.Rule;
import com.blamedcloud.parsertongue.grammar.annotations.AnnotationManager;

public class DependencyManager {

    public static String grammarFileToName(File grammarFile) {
        String fileName = grammarFile.getName();
        int extensionIndex = fileName.indexOf(".");
        if (extensionIndex > 0) {
            fileName = fileName.substring(0, extensionIndex);
        }
        return fileName;
    }

    private Map<String, File> grammarFiles;
    private Map<String, Grammar> grammars;

    private Tree<Grammar> primaryDependencyTree;

    private AnnotationManager primaryAnnotationManager;

    public DependencyManager(String mainGrammarName, Grammar mainGrammar, List<File> otherGrammarFiles) {
        primaryDependencyTree = new Tree<>(mainGrammarName, mainGrammar);
        grammarFiles = new HashMap<>();
        grammars = new HashMap<>();
        primaryAnnotationManager = mainGrammar.getAnnotationManager();
        for (File grammarFile : otherGrammarFiles) {
            String grammarName = grammarFileToName(grammarFile);
            grammarFiles.put(grammarName, grammarFile);
        }
    }

    public Grammar manage() {
        populateDependencies(primaryDependencyTree);
        resolveDependencies(primaryDependencyTree);
        return primaryDependencyTree.getData();
    }

    private void populateDependencies(Tree<Grammar> dependencyTree) {
        String grammarName = dependencyTree.getName();
        Set<String> dependencyNames = dependencyTree.getData().getDependencyNames();
        for (String dependencyName : dependencyNames) {
            if (containsGrammar(dependencyName)) {
                if (dependencyTree.hasAncestorWithName(dependencyName) || grammarName.equals(dependencyName)) {
                    throw new RuntimeException("Found recursive dependency: " + dependencyName);
                } else {
                    dependencyTree.addChild(new Tree<>(dependencyName, getGrammar(dependencyName)));
                }
            } else {
                throw new RuntimeException("Missing dependency: " + dependencyName);
            }
        }
        for (Tree<Grammar> child : dependencyTree) {
            populateDependencies(child);
        }
    }

    private Map<String, Map<String, Rule>> resolveDependencies(Tree<Grammar> dependencyTree) {
        if (!dependencyTree.getData().hasLinked()) {
            Map<String, Map<String, Rule>> neededRuleMaps = new HashMap<>();
            for (Tree<Grammar> child : dependencyTree) {
                Map<String, Map<String, Rule>> childRuleMaps = resolveDependencies(child);
                neededRuleMaps.putAll(childRuleMaps);
                neededRuleMaps.put(child.getName(), child.getData().getRuleMap());
            }
            dependencyTree.getData().setExternalRuleMaps(neededRuleMaps);
            dependencyTree.getData().linkRules();
        }
        return dependencyTree.getData().getExternalRuleMaps();
    }

    private boolean containsGrammar(String name) {
        return (grammars.containsKey(name) || grammarFiles.containsKey(name));
    }

    private Grammar getGrammar(String grammarName) {
        if (grammars.containsKey(grammarName)) {
            return grammars.get(grammarName);
        } else if (grammarFiles.containsKey(grammarName)) {
            File grammarFile = grammarFiles.get(grammarName);
            Grammar grammar = Grammar.newBuilder(grammarFile)
                                     .setDeferLinkage(true)
                                     .setAnnotationManager(primaryAnnotationManager)
                                     .build();
            grammars.put(grammarName, grammar);
            grammarFiles.remove(grammarName);
            return grammar;
        } else {
            throw new RuntimeException("Grammar with name: '" + grammarName + "' not found");
        }
    }

}
