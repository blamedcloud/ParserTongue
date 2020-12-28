package com.blamedcloud.parsertongue.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blamedcloud.parsertongue.grammar.expecterator.AlternationExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.ConcatenationExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.GroupExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.IdentifierExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.OptionalExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.ParseResultExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.RegexExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.RepeatExpecterator;
import com.blamedcloud.parsertongue.grammar.expecterator.TerminalExpecterator;
import com.blamedcloud.parsertongue.tokenizer.Token;
import com.blamedcloud.parsertongue.tokenizer.TokenType;
import com.blamedcloud.parsertongue.tokenizer.Tokenizer;

public class RHSTree {

    // all RHSTree's have a kind and a type
    private RHSKind levelKind;
    private RHSType levelType;

    // which of these are valid depends on
    // the levelKind and levelType of this tree.
    private List<RHSTree> children;
    private Token node;
    private TokenType regexNode;
    private Rule link;

    public RHSTree(RHSType type) {
        levelType = type;
        levelKind = type.getKind();
        children = new ArrayList<>();
        node = null;
        regexNode = null;
        link = null;
    }

    public void addChild(RHSTree child) {
        if (levelKind == RHSKind.LEAF) {
            throw new RuntimeException("Can't add child to LEAF kind RHSTree of Type: " + levelType);
        } else if ((levelKind == RHSKind.SINGLE) && (children.size() == 1)) {
            throw new RuntimeException("Can't add second child to SINGLE kind RHSTree of Type: " + levelType);
        } else {
            children.add(child);
        }
    }

    public void addLinkage(Map<String, Rule> ruleMap) {
        if (levelType == RHSType.IDENTIFIER) {
            if (node != null) {
                if (ruleMap.containsKey(node.getValue())) {
                    link = ruleMap.get(node.getValue());
                } else {
                    throw new RuntimeException("ERROR, Identifier: '" + node.getValue() + "' does not exist in rule mapping!");
                }
            } else {
                throw new RuntimeException("ERROR, node is still null!");
            }
        } else {
            for (RHSTree child : children) {
                child.addLinkage(ruleMap);
            }
        }
    }

    public Set<String> nonLinkedTerminals() {
        Set<String> terminals = new HashSet<>();
        if (levelType == RHSType.TERMINAL) {
            terminals.add(node.getValue());
        } else if (levelType != RHSType.IDENTIFIER) {
            for (RHSTree child : children) {
                Set<String> childTerminals = child.nonLinkedTerminals();
                terminals.addAll(childTerminals);
            }
        }
        return terminals;
    }

    public RHSTree popRightChild() {
        if (children.size() > 0) {
            return children.remove(children.size()-1);
        } else {
            throw new RuntimeException("Can't pop right child from empty child list!");
        }
    }

    public int size() {
        return children.size();
    }

    public void createNode(Token newNode) {
        if (node == null) {
            if (levelKind == RHSKind.LEAF) {
                node = newNode;
            } else {
                throw new RuntimeException("Can't create node on RHSTree of type: " + levelType);
            }
        } else {
            throw new RuntimeException("Can't create node because a node is already present!");
        }
    }

    public void createRegexNode(TokenType regex) {
        if (regexNode == null) {
            if (levelType == RHSType.REGEX) {
                regexNode = regex;
            } else {
                throw new RuntimeException("Can't create regex node on non-regex RHSType tree");
            }
        } else {
            throw new RuntimeException("Can't create regex node because it it already present!");
        }
    }

    public RHSType getType() {
        return levelType;
    }

    public RHSKind getKind() {
        return levelKind;
    }

    public RHSTree getChild() {
        return getChild(0);
    }

    public RHSTree getChild(int index) {
        return children.get(index);
    }

    public List<RHSTree> getChildren() {
        return children;
    }

    public Token getNode() {
        if (node != null) {
            return node;
        } else {
            throw new RuntimeException("node is null!");
        }
    }

    public TokenType getRegexNode() {
        if (regexNode != null) {
            return regexNode;
        } else {
            throw new RuntimeException("regex node is null!");
        }
    }

    public Rule getLink() {
        if (link != null) {
            return link;
        } else {
            throw new RuntimeException("link is null!");
        }
    }

    @Override
    public String toString() {
        return representation("", "    ");
    }

    public String representation(String previousIndent, String indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(previousIndent).append("RHSType: ").append(levelType).append("\n");
        sb.append(previousIndent).append("RHSKind: ").append(levelKind).append("\n");
        if (node != null) {
            sb.append(previousIndent).append("Node: ").append(node.toString()).append("\n");
        } else {
            sb.append(previousIndent).append("Children:\n");
            for (int i = 0; i < children.size(); i++) {
                sb.append(previousIndent).append(i).append(":\n");
                sb.append(children.get(i).representation(previousIndent+indent, indent));
            }
        }
        return sb.toString();
    }

    public ParseResultExpecterator getExpecterator(Tokenizer tokens) {
        if (levelType == RHSType.TERMINAL) {
            return new TerminalExpecterator(this, tokens);
        } else if (levelType == RHSType.REGEX) {
            return new RegexExpecterator(this, tokens);
        } else if (levelType == RHSType.IDENTIFIER) {
            return new IdentifierExpecterator(this, tokens);
        } else if (levelType == RHSType.GROUP) {
            return new GroupExpecterator(this, tokens);
        } else if (levelType == RHSType.OPTIONAL) {
            return new OptionalExpecterator(this, tokens);
        } else if (levelType == RHSType.ALTERNATION) {
            return new AlternationExpecterator(this, tokens);
        } else if (levelType == RHSType.REPEAT) {
            return new RepeatExpecterator(this, tokens);
        } else if (levelType == RHSType.CONCATENATION) {
            return new ConcatenationExpecterator(this, 0, tokens);
        }
        throw new RuntimeException("unknown level type");
    }

    public WalkResult walkTree(Set<String> previousIdentifiers) {
        boolean isInfinite = false;
        int treeSize = 0;

        if (levelType == RHSType.TERMINAL) {
            treeSize++;
        } else if (levelType == RHSType.IDENTIFIER) {
            return link.walk(previousIdentifiers);
        } else if (levelType == RHSType.REGEX) {
            // assume for simplicity's sake that this is an infinite regex
            isInfinite = true;
            treeSize = -1;
        } else if (levelType == RHSType.GROUP) {
            return children.get(0).walkTree(previousIdentifiers);
        } else if (levelType == RHSType.OPTIONAL) {
            return children.get(0).walkTree(previousIdentifiers);
        } else if (levelType == RHSType.REPEAT) {
            WalkResult result = children.get(0).walkTree(previousIdentifiers);
            if ((result.isInfinite) || (result.treeSize >= 1)) {
                isInfinite = true;
                treeSize = -1;
            }
        } else if (levelType == RHSType.CONCATENATION) {
            for (RHSTree child : children) {
                WalkResult childResult = child.walkTree(previousIdentifiers);
                if (childResult.isInfinite) {
                    isInfinite = true;
                    treeSize = -1;
                    break;
                } else {
                    treeSize += childResult.treeSize;
                }
            }
        } else if (levelType == RHSType.ALTERNATION) {
            for (RHSTree child : children) {
                WalkResult childResult = child.walkTree(previousIdentifiers);
                if (childResult.isInfinite) {
                    isInfinite = true;
                    treeSize = -1;
                    break;
                } else if (childResult.treeSize > treeSize) {
                    treeSize = childResult.treeSize;
                }
            }
        }
        return new WalkResult(isInfinite, treeSize);
    }

    // note that the copy returned will not be linked.
    public RHSTree copy() {
        RHSTree newTree = new RHSTree(levelType);

        if (levelKind == RHSKind.LIST || levelKind == RHSKind.SINGLE) {
            for (RHSTree child : children) {
                newTree.addChild(child.copy());
            }
        }

        if (levelKind == RHSKind.LEAF) {
            if (levelType == RHSType.REGEX) {
                newTree.createRegexNode(regexNode);
            } else {
                newTree.createNode(node);
            }
        }

        return newTree;
    }

}
