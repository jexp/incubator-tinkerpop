package com.tinkerpop.gremlin.client;

/** Enumeration of client operations -- useful to introspect a GremlinBuilder expression*/
public enum GremlinOp {
    START_GRAPH, MAP, FLAT_MAP, V, OUT, IN, BOTH, OUT_E, IN_E, IN_V, OUT_V, BOTH_V, PROPERTY, VALUE, BACK, PATH, BOTH_E, SELECT, COUNT, ITERATE, TO_LIST, DEDUP, SIMPLE_PATH, HAS_KEY, HAS_NOT_KEY, FILTER, HAS_VALUE, SIDE_EFFECT, GROUP_COUNT, LOOP, AS, NEXT, FILL;
}
