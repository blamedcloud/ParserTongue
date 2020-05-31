package com.blamedcloud.parsertongue.grammar.dependencies;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Tree<T> implements Iterable<Tree<T>> {

    private String name;
    private T data;
    private List<Tree<T>> children;
    private Tree<T> parent;

    public Tree(String name) {
        this.name = name;
        data = null;
        children = new ArrayList<>();
        parent = null;
    }

    public Tree(String name, T data) {
        this.name = name;
        this.data = data;
        children = new ArrayList<>();
        parent = null;
    }

    public Tree(String name, T data, List<Tree<T>> children) {
        this.name = name;
        this.data = data;
        this.children = children;
        parent = null;
    }

    public int size() {
        return children.size();
    }

    @Override
    public Iterator<Tree<T>> iterator() {
        return children.iterator();
    }

    public Tree<T> get(int index) {
        return children.get(index);
    }

    public String getName() {
        return name;
    }

    public T getData() {
        return data;
    }

    public void setData(T t) {
        data = t;
    }

    public void addChild(Tree<T> child) {
        child.setParent(this);
        children.add(child);
    }

    private void setParent(Tree<T> parent) {
        this.parent = parent;
    }

    public Tree<T> getParent() {
        return parent;
    }

    public boolean isRoot() {
        return (parent == null);
    }

    public boolean hasAncestorWithName(String ancestorName) {
        if (isRoot()) {
            return false;
        } else {
            if (parent.getName().equals(ancestorName)) {
                return true;
            } else {
                return parent.hasAncestorWithName(ancestorName);
            }
        }
    }

}
