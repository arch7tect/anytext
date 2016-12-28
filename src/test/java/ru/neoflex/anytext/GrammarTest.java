package ru.neoflex.anytext;

import com.sun.el.ValueExpressionLiteral;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import javax.el.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by orlov on 13.12.2016.
 */
public class GrammarTest {
    @Test
    public void feedLength() throws Exception {
        String grammarString = "{\"baseName\": \"any\", \"args\": {\"actual\":4, \"min\": \"${root.emitter.args.actual}\", \"max\": \"${root.emitter.args.actual}\"}}";
        Grammar grammar = new Grammar(grammarString);
        final String data = "qwertyuiop";
        List<Node> nodes = grammar.parse(data);
        Assert.assertThat(nodes.size(), CoreMatchers.is(1));
        Assert.assertThat(nodes.get(0).asString(), CoreMatchers.is("qwer"));
    }

    @Test
    public void feedEq() throws Exception {
        String grammarString = "{\"baseName\": \"eq\", \"args\": {\"value\":\"qwer\"}}";
        Grammar grammar = new Grammar(grammarString);
        final String data = "qwertyuiop";
        List<Node> nodes = grammar.parse(data);
        Assert.assertThat(nodes.size(), CoreMatchers.is(1));
        Assert.assertThat(nodes.get(0).asString(), CoreMatchers.is("qwer"));
    }

    @Test
    public void feedSeq() throws Exception {
        String grammarString = "{\"baseName\": \"test\", \"args\": {}, \"parsers\": [" +
                "{\"name\": \"test\", \"baseName\": \"seq\", \"args\": {\"parsers\": [" +
                "{\"baseName\": \"any\"}, " +
                "{\"baseName\": \"eq\", \"args\": {\"value\": \"ty\"}}, " +
                "{\"baseName\": \"any\"}, " +
                "{\"baseName\": \"eos\"}" +
                "]}}]}";
        Grammar grammar = new Grammar(grammarString);
        final String data = "qwertyuiop";
        List<Node> nodes = grammar.parse(data);
        Assert.assertThat(nodes.size(), CoreMatchers.is(1));
    }

    @Test
    public void feedOr() throws Exception {
        String grammarString = "{\"baseName\": \"test\", \"args\": {}, \"parsers\": [" +
                "{\"name\": \"test\", \"baseName\": \"or\", \"args\": {\"parsers\": [" +
                "{\"baseName\": \"eq\", \"args\": {\"value\": \"123\"}}, " +
                "{\"baseName\": \"eq\", \"args\": {\"value\": \"45\"}} " +
                "]}}]}";
        Grammar grammar = new Grammar(grammarString);
        List<Node> nodes1 = grammar.parse("123");
        Assert.assertThat(nodes1.size(), CoreMatchers.is(1));
        List<Node> nodes2 = grammar.parse("456");
        Assert.assertThat(nodes2.size(), CoreMatchers.is(1));
        List<Node> nodes3 = grammar.parse("789");
        Assert.assertThat(nodes3.size(), CoreMatchers.is(0));
    }

    @Test
    public void feedAnd() throws Exception {
        String grammarString = "{\"baseName\": \"and\", \"args\": {\"parsers\": [" +
                "{\"baseName\": \"any\"}, " +
                "{\"baseName\": \"eq\", \"args\": {\"value\": \"123\"}}" +
                "]}}";
        Grammar grammar = new Grammar(grammarString);
        List<Node> nodes1 = grammar.parse("123456");
        Assert.assertThat(nodes1.size(), CoreMatchers.is(1));
        Assert.assertThat(nodes1.get(0).asString(), CoreMatchers.is("123"));
    }

    @Test
    public void feedRepeat() throws Exception {
        String grammarString = "{\"baseName\": \"repeat\", \"args\": {\"parser\":" +
                "{\"baseName\": \"any\", \"args\":{\"min\": 2, \"max\": 2}}" +
                "}}";
        Grammar grammar = new Grammar(grammarString);
        List<Node> nodes1 = grammar.parse("12345");
        Assert.assertThat(nodes1.size(), CoreMatchers.is(2));
        Assert.assertThat(nodes1.get(0).asString(), CoreMatchers.is("12"));
    }

    @Test
    public void feedEmail() throws Exception {
        Path path = Paths.get(getClass().getResource("/ru/neoflex/anytext/email.json").toURI());
        String grammarString = new String(Files.readAllBytes(path), "UTF8");
        Grammar grammar = new Grammar(grammarString);
        List<Node> nodes = grammar.parse("EmailList", null,
                "very.unusual.\"@\\ \".unusual.com@strange.my-example.com," +
                        "very.\"(),:;<>[]\".VERY.\"very@\\\\\\ \\\"very\".unusual@[192.168.0.100]");
        Assert.assertThat(nodes.size(), CoreMatchers.is(1));
        List<Node> emails = nodes.get(0).collectByName("Email");
        Assert.assertThat(emails.size(), CoreMatchers.is(2));
    }

    @Test
    public void feedEmitter() throws Exception {
        Path path = Paths.get(getClass().getResource("/ru/neoflex/anytext/email.json").toURI());
        String grammarString = new String(Files.readAllBytes(path), "UTF8");
        Grammar grammar = new Grammar(grammarString);
        List<List> nodes = grammar.getEmitter("EmailList").parse(null,
                "very.unusual.\"@\\ \".unusual.com@strange.my-example.com," +
                        "very.\"(),:;<>[]\".VERY.\"very@\\\\\\ \\\"very\".unusual@[192.168.0.100]");
        Assert.assertThat(nodes.size(), CoreMatchers.is(2));
        Assert.assertThat(nodes.get(0).size(), CoreMatchers.is(3));
    }

    @Test
    public void feedCSV() throws Exception {
        Path path = Paths.get(getClass().getResource("/ru/neoflex/anytext/csv.json").toURI());
        String grammarString = new String(Files.readAllBytes(path), "UTF8");
        Grammar grammar = new Grammar(grammarString);
        Path dataPath = Paths.get(getClass().getResource("/ru/neoflex/anytext/data.csv").toURI());
        String data = new String(Files.readAllBytes(dataPath), "UTF8");
        List<List> nodes = grammar.getEmitter("CSV").parse(null, data);
        Assert.assertThat(nodes.size(), CoreMatchers.is(2));
        Assert.assertThat(nodes.get(0).size(), CoreMatchers.is(2));
    }

    @Test
    public void testEL() throws Exception {
        ExpressionFactory expressionFactory = ExpressionFactory.newInstance();
        ELContext simpleCtx = new StandardELContext(expressionFactory);
        VariableMapper variableMapper = simpleCtx.getVariableMapper();
        variableMapper.setVariable("i", new ValueExpressionLiteral(1, Long.class));
        ValueExpression valueExp = expressionFactory.createValueExpression(simpleCtx, "${i+1}", Object.class);
        Object obj = valueExp.getValue(simpleCtx);
        Assert.assertThat((Long)obj, CoreMatchers.is(2L));
    }

}