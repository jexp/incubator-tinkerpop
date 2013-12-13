package com.tinkerpop.gremlin.client;

/**
 * This class captures a variable referenced in a Gremlin Client
 */
public class GremlinVar {
    String varName;

    public GremlinVar(String varName) {
        this.varName = varName;
    }

    public String getVarName() {
        return varName;
    }
}
