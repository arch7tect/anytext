package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 15.12.2016.
 */
public class Or extends ParserListWrapperBase {
    Boolean reduced;

    public Or(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Or(repository, name, args);
            }
        });
    }

    @Override
    public void init(Node parent) {
        super.init(parent);
        reduced = parent.eval(getArgs().get("reduced"), Boolean.class, true);
    }

    public void feed(final Node parent, CharSequence data, final Consumer consumer) {
        for (Parser parser: getWrappedList()) {
            final int[] count = {0};
            final Node currentNode = new Node(this, parent, "");
            parser.init(currentNode);
            parser.feed(currentNode, data, new Consumer() {
                public void consume(Node node, CharSequence rest) {
                    currentNode.getChildren().add(node);
                    ++count[0];
                    Or.this.consume(currentNode.copyNormalize(), rest, consumer);
                    currentNode.getChildren().clear();
                }
            });
            if (reduced && count[0] > 0) {
                break;
            }
        }
    }
}
