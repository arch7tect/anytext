package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Option extends ParserWrapperBase {
    public Option(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Option(repository, name, args);
            }
        });
    }

    public void feed(Node parent, CharSequence data, final Consumer consumer) {
        final Node currentNode = new Node(this, parent, "");
        if (consume(currentNode, data, consumer)) {
            getWrapped().init(currentNode);
            getWrapped().feed(currentNode, data, new Consumer() {
                public boolean consume(Node node, CharSequence rest) {
                    currentNode.getChildren().add(node);
                    Option.this.consume(currentNode.copyNormalize(), rest, consumer);
                    currentNode.getChildren().clear();
                    return true;
                }
            });
        }
    }
}
