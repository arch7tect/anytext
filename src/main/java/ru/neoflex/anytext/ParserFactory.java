package ru.neoflex.anytext;

import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public interface ParserFactory {
    Parser newInstance(Repository repository, String name, Map<String, Object> args);
}
