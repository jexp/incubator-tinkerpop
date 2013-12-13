package com.tinkerpop.gremlin.client.executor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;
import com.tinkerpop.gremlin.client.netty.WebSocketConnection;

import java.net.URI;
import java.util.Iterator;
import java.util.Map;

/** This class implements a Gremlin evaluator that interacts with a Gremlin server over web-sockets */
public class WebSocketClient extends RemoteGremlinClient implements AutoCloseable {
    private URI uri;
    private WebSocketConnection conn;
    private boolean closed;

    public WebSocketClient(String uri) throws Exception {
        super(new ObjectMapper());

        conn = new WebSocketConnection(uri);

        conn.open();
        // TODO: Establish connection
    }

    @Override
    String evalAtServer(String requestJson) throws GremlinClientException {
        // This method is not used -- instead WebSocketClient directly overrides evalJson
        // The reason is that the current protocol is a streaming protocol -- not a request-response one
        // TODO: Remove this method based on request-response vs. streaming
        return null;
    }

    protected Iterator<JsonNode> evalJson(String graphName, Map<String, Object> bindingObjs, String script) throws RuntimeException {
        // TODO: Remove this after importing GremlinClient
        script = "com.tinkerpop.gremlin.pipes." + script + ".toList()";

        if (closed) {
            throw new GremlinClientException(GremlinClientErrorCodes.CONNECTION_ALREADY_CLOSED, "WebSocketClient.close() has been called on this object");
        }

        // TODO: graphName and bindingObjs are currently ignored -- must be included in the protocol
        return conn.eval(script);
    }

    @Override
    public void close() throws Exception {
        try {
            if (!closed) {
                conn.close();
            }
        } finally {
            closed = true;
        }
    }
}