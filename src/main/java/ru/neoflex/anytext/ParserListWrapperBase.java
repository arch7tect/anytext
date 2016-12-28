package ru.neoflex.anytext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by orlov on 21.12.2016.
 */
public abstract class ParserListWrapperBase extends ParserBase {
    List<Parser> wrappedList;

    public List<Parser> getWrappedList() {
        return wrappedList;
    }

    public void init(Node parent) {
        super.init(parent);
        wrappedList = new ArrayList<Parser>();
        List<Map<String, Object>> parsersArgs = (List<Map<String, Object>>)getArgs().get("parsers");
        for (Map<String, Object> parserArgs: parsersArgs) {
            Parser parser = getRepository().createFactory(parserArgs).newInstance(getRepository(), null, null);
            wrappedList.add(parser);
        }
    }

    public ParserListWrapperBase(Repository repository, String name, Map<String, Object> args) {
        super(repository, name, args);
    }
}
