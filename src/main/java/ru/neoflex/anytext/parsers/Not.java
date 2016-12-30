package ru.neoflex.anytext.parsers;

import ru.neoflex.anytext.*;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Not extends ParserWrapperBase {
    Integer min;
    Integer max;
    Boolean greedy;

    public Not(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public static void register(final Repository repository, final String name) {
        repository.registerFactory(name, new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                return new Not(repository, name, args);
            }
        });
    }

    @Override
    public void init(Node parent) {
        super.init(parent);
        min = parent.eval(getArgs().get("min"), Integer.class, 1);
        max = parent.eval(getArgs().get("max"), Integer.class, 1);
        greedy = parent.eval(getArgs().get("greedy"), Boolean.class, false);
    }

    public void feed(final Node parent, CharSequence data, final Consumer consumer) {
        boolean cont = true;
        final Node[] last = {null};
        for (int i = 1; i <= data.length() && (max < 0 || i <= max) && cont; ++i) {
            final int[] count = {0};
            getWrapped().init(parent);
            getWrapped().feed(parent, data.subSequence(i - 1, data.length()), new Consumer() {
                public boolean consume(Node node, CharSequence rest) {
                    ++count[0];
                    return false;
                }
            });
            if (count[0] > 0) {
                break;
            }
            last[0] = new Node(this, parent, data.subSequence(0, i));
            if (i >= min && !greedy) {
                cont = Not.this.consume(last[0], data.subSequence(i, data.length()), consumer);
            }
        }
        if (greedy && last[0] != null) {
            Not.this.consume(last[0], data.subSequence(last[0].getContent().length(), data.length()), consumer);
        }
    }
}
