package ru.neoflex.anytext;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.StandardELContext;
import javax.el.ValueExpression;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;

/**
 * Created by orlov on 13.12.2016.
 */
public class Node {
    private Parser emitter;
    private CharSequence content;
    private Node parent;

    private ELContext elContext;
    private static ExpressionFactory expressionFactory;

    private List<Node> children = new ArrayList<Node>();
    private Object info;

    public Node(Parser emitter, Node parent, CharSequence content) {
        this(emitter, parent, content, null);
    }

    public Node(Parser emitter, Node parent, CharSequence content, Object info) {
        this.emitter = emitter;
        this.parent = parent;
        this.content = content;
        this.info = info;
    }

    public Parser getEmitter() {
        return emitter;
    }

    public void setEmitter(Parser emitter) {
        this.emitter = emitter;
    }

    public CharSequence getContent() {
        return content;
    }

    public void setContent(CharSequence content) {
        this.content = content;
    }

    public List<Node> getChildren() {
        return children;
    }

    public Object getInfo() {
        return info;
    }

    public void setInfo(Object info) {
        this.info = info;
    }

    public Node getParent() {
        return parent;
    }
    public Node getRoot() {
        if (getParent() == null) {
            return this;
        }
        return getParent().getRoot();
    }
    public String getName() {
        if (getEmitter() == null) {
            return "";
        }
        return getEmitter().getName();
    }
    public String getFullName() {
        if (getParent() == null) {
            return getName();
        }
        return getParent().getName() + "/" + getName();
    }

    public<T> T eval(Object expression, Class<T> type, Object def) {
        return eval(expression != null ? expression : def, type);
    }

    public<T> T eval(Object expression, Class<T> type) {
        Node root = getRoot();
        ELContext elContext = root.getELContext();
        if (!(expression instanceof String)) {
            return (T)elContext.convertToType(expression, type);
        }
        elContext.getELResolver().setValue(elContext, null, "node", this);
        ValueExpression valueExp = getExpressionFactory().createValueExpression(elContext, (String)expression, type);
        return (T)valueExp.getValue(elContext);
    }

    private static ExpressionFactory getExpressionFactory() {
        if (expressionFactory == null) {
            expressionFactory = ExpressionFactory.newInstance();
        }
        return expressionFactory;
    }

    private ELContext getELContext() {
        if (elContext == null) {
            elContext = new StandardELContext(getExpressionFactory());
            elContext.getELResolver().setValue(elContext, null, "root", getRoot());
        }
        return elContext;
    }

    public Node copy() {
        Node node = new Node(this.getEmitter(), this.getParent(), this.getContent(), this.getInfo());
        node.getChildren().addAll(this.getChildren());
        return node;
    }

    public Node copyNormalize() {
        Node node = this.copy();
        StringBuffer sb = new StringBuffer();
        for (Node child: this.getChildren()) {
            sb.append(child.getContent());
        }
        node.setContent(sb.toString());
        return node;
    }

    public List<Node> collectByName(String name, List<Node> seen) {
        if (name.equals(getName())) {
            seen.add(this);
        }
        for (Node node: getChildren()) {
            node.collectByName(name, seen);
        }
        return seen;
    }

    public List<Node> collectByName(String name) {
        return collectByName(name, new ArrayList<Node>());
    }

    public Integer asInteger() {
        return Integer.parseInt(asString());
    }

    public Double asDouble() {
        return Double.parseDouble(asString());
    }

    public BigDecimal asDecimal() {
        return new BigDecimal(asString());
    }

    public Date asDate() {
        return Date.valueOf(asString());
    }

    public Timestamp asTimestamp() {
        return Timestamp.valueOf(asString());
    }

    public String asString() {
        return getContent().toString();
    }

    public String toString() {
        return getName() + "[" + asString().substring(0, min(32, getContent().length())) + "]";
    }
}
