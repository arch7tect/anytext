package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Repeat extends ParserWrapperBase {
    Node currentNode;
    Node lastNode;
    Integer min;
    Integer max;
    Boolean greedy;

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
        greedy = parent.eval(getArgs().get("greedy"), Boolean.class, false);
        currentNode = new Node(this, parent, "");
    }

    public void feed(final Node parent, CharSequence data, final Consumer consumer) {
        if (currentNode.getChildren().size() == 0 && min == 0) {
            if (!consume(currentNode.copyNormalize(), data, consumer)) {
                return;
            }
        }
        Parser wrapped = createWrapped();
        wrapped.init(currentNode);
        wrapped.feed(currentNode, data, new Consumer() {
            public boolean consume(Node node, CharSequence rest) {
                currentNode.getChildren().add(node);
                lastNode = currentNode.copyNormalize();
                boolean cont = true;
                if (!greedy && currentNode.getChildren().size() >= min) {
                    cont = Repeat.this.consume(lastNode, rest, consumer);
                }
                if (cont && (max < 0 || currentNode.getChildren().size() <= max)) {
                    feed(parent, rest, consumer);
                }
                currentNode.getChildren().remove(currentNode.getChildren().size() - 1);
                return true;
            }
        });
        if (greedy && currentNode.getChildren().size() == 0 && lastNode != null) {
            this.consume(lastNode, data.subSequence(lastNode.getContent().length(), data.length()), consumer);
        }
    }
}
