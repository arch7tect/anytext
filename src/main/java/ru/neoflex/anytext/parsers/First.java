package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class First extends ParserWrapperBase {
    public First(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new First(repository, name, args);
            }
        });
    }

    public void feed(final Node parent, CharSequence data, final Consumer consumer) {
        final Node currentNode = new Node(this, parent, "");
        getWrapped().init(currentNode);
        getWrapped().feed(currentNode, data, new Consumer() {
            public boolean consume(Node node, CharSequence rest) {
                if (currentNode.getChildren().size() == 0) {
                    currentNode.getChildren().add(node);
                    First.this.consume(currentNode.copyNormalize(), rest, consumer);
                }
                return false;
            }
        });
    }
}
