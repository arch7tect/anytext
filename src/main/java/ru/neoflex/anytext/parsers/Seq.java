package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 15.12.2016.
 */
public class Seq extends ParserListWrapperBase {
    Node currentNode;

    public Seq(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Seq(repository, name, args);
            }
        });
    }

    public void init(Node parent) {
        super.init(parent);
        currentNode = new Node(this, parent, "");
    }

    public void feed(final Node parent, CharSequence data, final Consumer consumer) {
        if (currentNode.getChildren().size() >= getWrappedList().size()) {
            return;
        }
        Parser parser = getWrappedList().get(currentNode.getChildren().size());
        parser.init(currentNode);
        parser.feed(currentNode, data, new Consumer() {
            public boolean consume(Node node, CharSequence rest) {
                currentNode.getChildren().add(node);
                if (currentNode.getChildren().size() >= getWrappedList().size()) {
                    Seq.this.consume(currentNode.copyNormalize(), rest, consumer);
                }
                else {
                    feed(currentNode, rest, consumer);
                }
                currentNode.getChildren().remove(currentNode.getChildren().size() - 1);
                return true;
            }
        });
    }
}
