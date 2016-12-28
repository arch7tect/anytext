package ru.neoflex.anytext;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public abstract class ParserBase implements Parser {
    private String name;
    private String checkExpr;

    private Repository repository;

    private Map<String, Object> args;

    public ParserBase(Repository repository, String name, Map<String, Object> args) {
        this.repository = repository;
        this.name = name;
        this.args = args;
    }

    public Map<String, Object> getArgs() {
        return args;
    }

    public Repository getRepository() {
        return repository;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void init(Node parent) {
        checkExpr = (String) getArgs().get("check");
    }

    public void consume(Node node, CharSequence rest, Consumer consumer) {
        if (checkExpr != null) {
            if (!node.eval(checkExpr, Boolean.class)) {
                return;
            }
        }
        consumer.consume(node, rest);
    }

    public String toString() {
        return getClass().getSimpleName() + "[" + getName() + "]";
    }
}
