package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Any extends ParserBase {
    Integer min;
    Integer max;

    public Any(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Any(repository, name, args);
            }
        });
    }

    @Override
    public void init(Node parent) {
        super.init(parent);
        min = parent.eval(getArgs().get("min"), Integer.class, 0);
        max = parent.eval(getArgs().get("max"), Integer.class, -1);

    }

    public void feed(Node parent, CharSequence data, Consumer consumer) {
        boolean cont = true;
        for (int i = min; i <= data.length() && (max < 0 || i <= max) && cont; ++i) {
            Node node = new Node(this, parent, data.subSequence(0, i));
            cont = consume(node, data.subSequence(i, data.length()), consumer);
        }
    }
}
