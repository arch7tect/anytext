package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Repeat extends ParserWrapperBase {
    Node currentNode;
    Integer min;
    Integer max;

    public Repeat(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Repeat(repository, name, args);
            }
        });
    }

    public void init(Node parent) {
        super.init(parent);
        min = parent.eval(getArgs().get("min"), Integer.class, 1);
        max = parent.eval(getArgs().get("max"), Integer.class, -1);
        currentNode = new Node(this, parent, "");
    }

    public void feed(final Node parent, CharSequence data, final Consumer consumer) {
        if (currentNode.getChildren().size() == 0 && min == 0) {
            consume(currentNode.copyNormalize(), data, consumer);
        }
        Parser wrapped = createWrapped();
        wrapped.init(currentNode);
        wrapped.feed(currentNode, data, new Consumer() {
            public void consume(Node node, CharSequence rest) {
                currentNode.getChildren().add(node);
                if (currentNode.getChildren().size() >= min) {
                    Repeat.this.consume(currentNode.copyNormalize(), rest, consumer);
                }
                if (max < 0 || currentNode.getChildren().size() <= max) {
                    feed(parent, rest, consumer);
                }
                currentNode.getChildren().remove(currentNode.getChildren().size() - 1);
            }
        });
    }
}
