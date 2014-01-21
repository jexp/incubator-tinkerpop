package com.tinkerpop.gremlin.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.tinkerpop.gremlin.Gremlin;
import com.tinkerpop.gremlin.Pipe;
import com.tinkerpop.gremlin.Pipeline;
import com.tinkerpop.gremlin.oltp.map.IdentityPipe;
import com.tinkerpop.gremlin.util.HolderIterator;

import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/** This class is an expression builder for Gremlin and runs the query against a GremlinClient */
public class GremlinBuilder {
    String graph;
    String query;
    Object[] args;
    GremlinOp operation;
    GremlinBuilder subExpr;

    protected GremlinBuilder(String query, GremlinOp operation, Object[] args, GremlinBuilder subExpr) {
        this.query = query;
        this.args = args;
        this.operation = operation;
        this.subExpr = subExpr;
    }

    public String toString() {
        return query;
    }

    public String getGraph() {
        if (operation == GremlinOp.START_GRAPH) {
            return (String)args[0];
        } else {
            return subExpr.getGraph();
        }
    }

    public GremlinOp getOperation() {
        return operation;
    }

    public Object[] getArgs() {
        return args;
    }

    public static GremlinBuilder of(String graphName) {
        return new GremlinBuilder("Gremlin.of(" + graphName + ")", GremlinOp.START_GRAPH, new Object[] {graphName}, null);
    }

    public static GremlinVar var(String varName) {
        return new GremlinVar(varName);
    }

    public GremlinBuilder identity() {
        return this;
    }

    public GremlinBuilder map(String function) {
        return new GremlinBuilder(query + ".map({" + function + "})", GremlinOp.MAP, new Object[] {function}, this);
    }

    public GremlinBuilder flatMap(String function) {
        return new GremlinBuilder(query + ".flatMap({" + function + "})", GremlinOp.FLAT_MAP, new Object[] {function}, this);
    }

    public GremlinBuilder V() {
        return new GremlinBuilder(query + ".V()", GremlinOp.V, new Object[] {}, this);
    }

    private String quote(String str) {
        // Escaping str within single quotes
        return "'" + escapeGroovy(str) + "'";
    }

    private String escapeGroovy(String input) {
        StringWriter result = new StringWriter();

        // Adapted from Groovy's StringEscapeUtils.escapeJavascript
        int sz = input.length();
        for (int i = 0; i < sz; i++) {
            char ch = input.charAt(i);

            // handle unicode
            if (ch > 0xfff) {
                result.write("\\u" + hex(ch));
            } else if (ch > 0xff) {
                result.write("\\u0" + hex(ch));
            } else if (ch > 0x7f) {
                result.write("\\u00" + hex(ch));
            } else if (ch < 32) {
                switch (ch) {
                    case '\b' :
                        result.write('\\');
                        result.write('b');
                        break;
                    case '\n' :
                        result.write('\\');
                        result.write('n');
                        break;
                    case '\t' :
                        result.write('\\');
                        result.write('t');
                        break;
                    case '\f' :
                        result.write('\\');
                        result.write('f');
                        break;
                    case '\r' :
                        result.write('\\');
                        result.write('r');
                        break;
                    default :
                        if (ch > 0xf) {
                            result.write("\\u00" + hex(ch));
                        } else {
                            result.write("\\u000" + hex(ch));
                        }
                        break;
                }
            } else {
                switch (ch) {
                    case '\'' :
                        result.write('\\');
                        result.write('\'');
                        break;
                    case '"' :
                        result.write('\\');
                        result.write('"');
                        break;
                    case '\\' :
                        result.write('\\');
                        result.write('\\');
                        break;
                    default :
                        result.write(ch);
                        break;
                }
            }
        }

        return result.toString();
    }

    private static String hex(char ch) {
        return Integer.toHexString(ch).toUpperCase(Locale.ENGLISH);
    }

    private String csv(String[] labels) {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for (String label : labels) {
            if (first) {
                first = false;
            } else {
                result.append(',');
            }

            result.append(quote(label));
        }
        return result.toString();
    }

    public GremlinBuilder out(String... labels) {
        return new GremlinBuilder(query + ".out(" + csv(labels) + ")", GremlinOp.OUT, labels, this);
    }

    public GremlinBuilder in(String... labels) {
        return new GremlinBuilder(query + ".in(" + csv(labels) + ")", GremlinOp.IN, labels, this);
    }

    public GremlinBuilder both(String... labels) {
        return new GremlinBuilder(query + ".both(" + csv(labels) + ")", GremlinOp.BOTH, labels, this);
    }

    public GremlinBuilder outE(String... labels) {
        return new GremlinBuilder(query + ".outE(" + csv(labels) + ")", GremlinOp.OUT_E, labels, this);
    }

    public GremlinBuilder inE(String... labels) {
        return new GremlinBuilder(query + ".inE(" + csv(labels) + ")", GremlinOp.IN_E, labels, this);
    }

    public GremlinBuilder bothE(String... labels) {
        return new GremlinBuilder(query + ".bothE(" + csv(labels) + ")", GremlinOp.BOTH_E, labels, this);
    }

    public GremlinBuilder inV() {
        return new GremlinBuilder(query + ".inV()", GremlinOp.IN_V, new String[0], this);
    }

    public GremlinBuilder outV() {
        return new GremlinBuilder(query + ".outV()", GremlinOp.OUT_V, new String[0], this);
    }

    public GremlinBuilder bothV() {
        return new GremlinBuilder(query + ".bothV()", GremlinOp.BOTH_V, new String[0], this);
    }

    public GremlinBuilder property(String key) {
        return new GremlinBuilder(query + ".property(" + quote(key) + ")", GremlinOp.PROPERTY, new Object[] {key}, this);
    }

    public GremlinBuilder value(String key) {
        return new GremlinBuilder(query + ".value(" + quote(key) + ")", GremlinOp.VALUE, new Object[] {key}, this);
    }

    public GremlinBuilder path() {
        return new GremlinBuilder(query + ".path()", GremlinOp.PATH, new String[0], this);
    }

    public GremlinBuilder back(String name) {
        return new GremlinBuilder(query + ".back(" + quote(name) + ")", GremlinOp.BACK, new Object[] {name}, this);
    }

    public GremlinBuilder match(String inAs, String outAs, Pipeline... pipelines) {
        // TODO: Not supported yet
        throw new GremlinClientException(GremlinClientErrorCodes.INTERNAL_ERROR, "Unsupported operation: match");
    }

    public GremlinBuilder select(String... names) {
        return new GremlinBuilder(query + ".select(" + csv(names) + ")", GremlinOp.SELECT, names, this);
    }

    public GremlinBuilder filter(String predicate) {
        return new GremlinBuilder(query + ".filter({" + predicate + "})", GremlinOp.FILTER, new Object[] {predicate}, this);
    }

    public GremlinBuilder simplePath() {
        return new GremlinBuilder(query + ".simplePath()", GremlinOp.SIMPLE_PATH, new String[0], this);
    }

    public GremlinBuilder has(String key) {
        return new GremlinBuilder(query + ".has(" + quote(key) + ")", GremlinOp.HAS_KEY, new Object[] {key}, this);
    }

    public GremlinBuilder hasNot(String key) {
        return new GremlinBuilder(query + ".hasNot(" + quote(key) + ")", GremlinOp.HAS_NOT_KEY, new Object[] {key}, this);
    }

    public GremlinBuilder has(String key, String value) {
        return new GremlinBuilder(query + ".has(" + quote(key) + "," + quote(value) + ")", GremlinOp.HAS_VALUE, new Object[] {key, value}, this);
    }

    public GremlinBuilder has(String key, BigDecimal value) {
        return new GremlinBuilder(query + ".has(" + quote(key) + "," + value + ")", GremlinOp.HAS_VALUE, new Object[] {key, value}, this);
    }

    public GremlinBuilder has(String key, GremlinVar var) {
        return new GremlinBuilder(query + ".has(" + quote(key) + "," + var.getVarName() + ")", GremlinOp.HAS_VALUE, new Object[] {key, var.getVarName()}, this);
    }

    public GremlinBuilder dedup() {
        return new GremlinBuilder(query + ".dedup()", GremlinOp.DEDUP, new String[0], this);
    }

    public GremlinBuilder dedup(String function) {
        return new GremlinBuilder(query + ".dedup({" + function + "})", GremlinOp.DEDUP, new Object[] {function}, this);
    }

    public GremlinBuilder sideEffect(String function) {
        return new GremlinBuilder(query + ".sideEffect({" + function + "})", GremlinOp.SIDE_EFFECT, new Object[] {function}, this);
    }

    public GremlinBuilder groupCount() {
        return new GremlinBuilder(query + ".groupCount()", GremlinOp.GROUP_COUNT, new String[0], this);
    }

    public GremlinBuilder next(int amount) {
        return new GremlinBuilder(query + ".next(" + amount + ")", GremlinOp.NEXT, new Object[] {new Integer(amount)}, this);
    }

    // TODO: Is this required?
    public GremlinBuilder toList() {
        return new GremlinBuilder(query + ".toList()", GremlinOp.TO_LIST, new String[0], this);
    }

    public GremlinBuilder fill(GremlinVar var) {
        return new GremlinBuilder(query + ".fill(" + quote(var.getVarName()) + ")", GremlinOp.FILL, new Object[] {var}, this);
    }

    public GremlinBuilder iterate() {
        return new GremlinBuilder(query + ".iterate()", GremlinOp.ITERATE, new String[0], this);
    }

    public GremlinBuilder count() {
        return new GremlinBuilder(query + ".count()", GremlinOp.COUNT, new String[0], this);
    }

    public GremlinBuilder as(String name) {
        return new GremlinBuilder(query + ".as(" + quote(name) + ")", GremlinOp.AS, new Object[] {name}, this);
    }

    public GremlinBuilder loop(String name, String whilePredicate, String emitPredicate) {
        return new GremlinBuilder(query + ".loop(" + quote(name) + "," + whilePredicate + "," + emitPredicate + ")",
                GremlinOp.LOOP, new Object[] {name, whilePredicate, emitPredicate}, this);
    }

    // TODO: See if methods like submit(), pre(), post() are needed

    // METHODS TO EVALUATE THE QUERY
    public Pipeline submit(GremlinClient executor, Map<String, Object> bindings) {
        return pipeline(executor.eval(getGraph(), bindings, query));
    }

    public Pipeline submit(GremlinClient executor, Map<String, Object> bindings, Class itemClass) {
        return pipeline(executor.eval(getGraph(), bindings, query, itemClass));
    }

    public Pipeline submit(GremlinClient executor, Map<String, Object> bindings, TypeReference itemType) {
        return pipeline(executor.eval(getGraph(), bindings, query, itemType));
    }

    private Pipeline pipeline(Iterator<Object> starts) {
        Pipeline ans = Gremlin.of();
        Pipe pipe = new IdentityPipe(ans);
        pipe.addStarts(new HolderIterator(pipe, starts, false));
        return ans.addPipe(pipe);
    }

    // Methods without bindings
    public Pipeline submit(GremlinClient executor) {
        return pipeline(executor.eval(getGraph(), Collections.emptyMap(), query));
    }

    public Pipeline submit(GremlinClient executor, Class itemClass) {
        return pipeline(executor.eval(getGraph(), Collections.emptyMap(), query, itemClass));
    }

    public Pipeline submit(GremlinClient executor, TypeReference itemType) {
        return pipeline(executor.eval(getGraph(), Collections.emptyMap(), query, itemType));
    }
}
