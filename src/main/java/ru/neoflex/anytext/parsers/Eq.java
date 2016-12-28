package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Eq extends ParserBase {
    String value;

    public Eq(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Eq(repository, name, args);
            }
        });
    }

    public void init(Node parent) {
        super.init(parent);
        value = parent.eval(getArgs().get("value"), String.class);
        if (value.length() == 0) {
            throw new RuntimeException("eq length==0");
        }
    }

    public void feed(Node parent, CharSequence data, Consumer consumer) {
        if (data.length() >= value.length() && value.equals(data.subSequence(0, value.length()).toString())) {
            consume(new Node(this, parent, data.subSequence(0, value.length())), data.subSequence(value.length(), data.length()), consumer);
        }
    }
}
