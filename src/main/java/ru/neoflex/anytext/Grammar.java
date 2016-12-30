package ru.neoflex.anytext;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by orlov on 13.12.2016.
 */
public class Grammar {
    private Repository repository;
    private Map<String, Object> grammarArgs;
    private Map<String, Emitter> emitters = new HashMap<String, Emitter>();

    public Emitter getEmitter(String name) {
        return emitters.get(name);
    }

    public Grammar(Repository baseRepository, Map<String, Object> grammarArgs) {
        if (baseRepository == null) {
            baseRepository = Repository.getBASE();
        }
        repository = Repository.newInstance(baseRepository);
        this.grammarArgs = grammarArgs;
        List<Map<String, Object>> parsersArgs = (List<Map<String, Object>>)grammarArgs.get("parsers");
        if (parsersArgs != null) {
            repository.registerParsers(parsersArgs);
        }
        List<Map<String, Object>> emittersArgs = (List<Map<String, Object>>)grammarArgs.get("emitters");
        if (emittersArgs != null) {
            for (Map<String, Object> emitterArgs: emittersArgs) {
                Emitter emitter = new Emitter(this, emitterArgs);
                emitters.put(emitter.name, emitter);
            }
        }
    }

    private static Map<String, Object> readArgs(String jsonString) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
            return objectMapper.readValue(jsonString, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Grammar(String jsonString) {
        this(null, readArgs(jsonString));
    }

    public Repository getRepository() {
        return repository;
    }

    private Parser createParser(String baseName, Map<String, Object> args) {
        return getRepository().getFactory(baseName).newInstance(getRepository(), null, args);
    }

    public void feed(String baseName, Map<String, Object> args, CharSequence data, Consumer consumer) {
        Parser rootParser = createParser(baseName, args);
        Node rootNode = new Node(rootParser, null, "");
        rootParser.init(rootNode);
        rootParser.feed(rootNode, new CharSequenceView(data), consumer);
    }

    public List<Node> parse(CharSequence data) {
        String baseName = (String) grammarArgs.get("baseName");
        return parse(baseName, (Map<String, Object>) grammarArgs.get("args"), data);
    }

    public List<Node> parse(String baseName, Map<String, Object> args, CharSequence data) {
        final List<Node> result = new ArrayList<Node>();
        feed(baseName, args, data, new Consumer() {
            public boolean consume(Node node, CharSequence rest) {
                result.add(node);
                return true;
            }
        });
        return result;
    }

    public interface Walker<T> {
        void walk(T node);
    }
    public static class Emitter {
        Grammar grammar;
        private String name;
        private String parser;
        private String rowName;
        private Object isRowExp;
        public class Column {
            String columnName;
            Object isColumnExpr;
            Object indexExpr;
            Object valueExp;
        }
        private List<Column> columns = new ArrayList<Column>();

        public Emitter(Grammar grammar, Map<String, Object> args) {
            this.grammar = grammar;
            name = (String) args.get("name");
            parser = (String) args.get("parser");
            rowName = (String) args.get("rowName");
            isRowExp = args.get("isRow");
            List<Map<String, Object>> columnsArgs = (List<Map<String, Object>>) args.get("columns");
            for (Map<String, Object> columnArgs: columnsArgs) {
                Column column = new Column();
                column.columnName = (String) columnArgs.get("columnName");
                column.isColumnExpr = columnArgs.get("isColumn");
                column.indexExpr = columnArgs.get("index");
                column.valueExp = columnArgs.get("value");
                columns.add(column);
            }
        }

        public void feed(Map<String, Object> args, CharSequence data, final Walker<List> walker) {
            grammar.feed(parser, args, data, new Consumer() {
                public boolean consume(Node node, CharSequence rest) {
                    collectRows(node, walker);
                    return true;
                }
            });
        }

        public List<List> parse(Map<String, Object> args, CharSequence data) {
            final List result = new ArrayList();
            feed(args, data, new Walker<List>() {
                public void walk(List node) {
                    result.add(node);
                }
            });
            return result;
        }

        public String getName() {
            return name;
        }

        public void collectRows(Node node, Walker<List> walker) {
            Boolean isRow = (rowName != null && rowName.equals(node.getName())) || node.eval(isRowExp, Boolean.class, false);
            if (isRow) {
                List<Object> row = new ArrayList<Object>();
                for (int i = 0; i < columns.size(); ++i) {
                    Column column = columns.get(i);
                    collectColumn(row, i, column, node);
                }
                walker.walk(row);
            }
            else {
                for (Node child: node.getChildren()) {
                    collectRows(child, walker);
                }
            }
        }

        private void collectColumn(List<Object> row, int i, Column column, Node node) {
            Boolean isColumn = (column.columnName != null && column.columnName.equals(node.getName())) || node.eval(column.isColumnExpr, Boolean.class, false);
            if (isColumn) {
                Integer index = node.eval(column.indexExpr, Integer.class, i);
                if (index < 0) {
                    index = row.size();
                }
                while (row.size() <= index) {
                    row.add(null);
                }
                Object value = node.eval(column.valueExp, Object.class, node.asString());
                row.set(index, value);
            }
            for (Node child: node.getChildren()) {
                collectColumn(row, i, column, child);
            }
        }
    }
}
