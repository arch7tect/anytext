package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Eos extends ParserBase {

    public Eos(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Eos(repository, name, args);
            }
        });
    }

    public void feed(Node parent, CharSequence data, Consumer consumer) {
        if (data.length() == 0) {
            consume(new Node(this, parent, ""), "", consumer);
        }
    }
}
