package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 15.12.2016.
 */
public class And extends ParserListWrapperBase {
    Node currentNode;

    public And(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new And(repository, name, args);
            }
        });
    }

    public void init(Node parent) {
        super.init(parent);
        currentNode = new Node(this, parent, "");
    }

    public void feed(final Node parent, final CharSequence data, final Consumer consumer) {
        if (currentNode.getChildren().size() >= getWrappedList().size()) {
            return;
        }
        Parser parser = getWrappedList().get(currentNode.getChildren().size());
        parser.init(currentNode);
        parser.feed(currentNode, data, new Consumer() {
            public boolean consume(Node node, CharSequence rest) {
                if (currentNode.getChildren().size() == 0) {
                    currentNode.setContent(node.getContent());
                }
                else if (currentNode.getContent().length() < node.getContent().length()) {
                    return true;
                }
                else if (currentNode.getContent().length() > node.getContent().length()) {
                    return false;
                }
                currentNode.getChildren().add(node);
                if (currentNode.getChildren().size() >= getWrappedList().size()) {
                    And.this.consume(currentNode.copy(), rest, consumer);
                }
                else {
                    feed(currentNode, data, consumer);
                }
                currentNode.getChildren().remove(currentNode.getChildren().size() - 1);
                return true;
            }
        });
    }
}
