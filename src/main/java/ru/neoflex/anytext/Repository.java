package ru.neoflex.anytext;

import ru.neoflex.anytext.parsers.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Repository {
    private static Repository BASE;

    private Repository parent;
    private Map<String, ParserFactory> factoryMap = new HashMap<String, ParserFactory>();

    private Repository() {
    }

    private Repository(Repository parent) {
        this.parent = parent;
    }

    public static Repository newInstance() {
        return new Repository(BASE);
    }

    public static  Repository newInstance(Repository parent) {
        return new Repository(parent);
    }

    public void registerFactory(String name, ParserFactory parserFactory) {
        factoryMap.put(name, parserFactory);
    }

    public void registerParsers(List<Map<String, Object>> parsersArgs) {
        for (Map<String, Object> parserArgs: parsersArgs) {
            registerFactory(parserArgs);
        }
    }

    protected ParserFactory getFactory(String name) {
        ParserFactory parserFactory = factoryMap.get(name);
        if (parserFactory == null) {
            if (parent == null) {
                throw new RuntimeException("parser not found " + name);
            }
            parserFactory = parent.getFactory(name);
        }
        return parserFactory;
    }

    public ParserFactory createFactory(Map<String, Object> parserArgs) {
        return createFactory(parserArgs, false);
    }
    public void registerFactory(Map<String, Object> parserArgs) {
        createFactory(parserArgs, true);
    }
    public ParserFactory createFactory(Map<String, Object> parserArgs, boolean register) {
        final String newName = (String)parserArgs.get("name");
        final String baseName = (String)parserArgs.get("baseName");
        final Map<String, Object> baseArgs = (Map<String, Object>)parserArgs.get("args");
        ParserFactory newFactory = new ParserFactory() {
            public Parser newInstance(Repository repository, String name, Map<String, Object> args) {
                Map<String, Object> actualArgs = new HashMap<String, Object>();
                if (baseArgs != null) {
                    actualArgs.putAll(baseArgs);
                }
                if (args != null) {
                    actualArgs.putAll(args);
                }
                String actualName = name != null ? name : (newName != null ? newName : baseName);
                ParserFactory baseFactory = repository.getFactory(baseName);
                Parser parser = baseFactory.newInstance(repository, actualName, actualArgs);
                return parser;
            }
        };
        if (register) {
            registerFactory(newName, newFactory);
        }
        return newFactory;
    }

    public static Repository getBASE() {
        if (BASE == null) {
            BASE = new Repository();
            Eq.register(BASE, "eq");
            Any.register(BASE, "any");
            Seq.register(BASE, "seq");
            Eos.register(BASE, "eos");
            Option.register(BASE, "option");
            Or.register(BASE, "or");
            And.register(BASE, "and");
            Set.register(BASE, "set");
            Repeat.register(BASE, "repeat");
            First.register(BASE, "first");
            Last.register(BASE, "last");
            Not.register(BASE, "not");
        }
        return BASE;
    }
}
