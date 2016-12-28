package ru.neoflex.anytext;

import java.util.Map;

/**
 * Created by orlov on 21.12.2016.
 */
public abstract class ParserWrapperBase extends ParserBase {
    private Parser wrapped;

    public ParserWrapperBase(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }

    public Parser getWrapped() {
        return wrapped;
    }

    public void init(Node parent) {
        super.init(parent);
        wrapped = createWrapped();
    }

    protected Parser createWrapped() {
        Map<String, Object> parserArgs = (Map<String, Object>)getArgs().get("parser");
        if (parserArgs == null) {
            throw new RuntimeException("embedded parser not found for " + getName());
        }
        return getRepository().createFactory(parserArgs).newInstance(getRepository(), null, null);
    }
}
