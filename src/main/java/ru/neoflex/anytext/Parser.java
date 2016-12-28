package ru.neoflex.anytext;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public interface Parser {
    Repository getRepository();
    String getName();
    void setName(String name);
    Map<String, Object> getArgs();
    void feed(Node parent, CharSequence data, Consumer consumer);
    void init(Node parent);
}
