package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Set extends ParserBase {
    java.util.Set<Character> set;
    Integer min;
    Integer max;

    public Set(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Set(repository, name, args);
            }
        });
    }

    public void init(Node parent) {
        super.init(parent);
        String chars = parent.eval(getArgs().get("chars"), String.class);
        set = new HashSet<Character>();
        for (int i = 0; i < chars.length(); ++i) {
            set.add(chars.charAt(i));
        }
        min = parent.eval(getArgs().get("min"), Integer.class, 1);
        max = parent.eval(getArgs().get("max"), Integer.class, 1);
    }

    public void feed(Node parent, CharSequence data, Consumer consumer) {
        for (int i = 1; i <= data.length() && (max < 0 || i <= max); ++i) {
            if (!set.contains(data.charAt(i - 1))) {
                break;
            }
            if (i >= min) {
                Node node = new Node(this, parent, data.subSequence(0, i));
                consume(node, data.subSequence(i, data.length()), consumer);
            }
        }
    }
}
