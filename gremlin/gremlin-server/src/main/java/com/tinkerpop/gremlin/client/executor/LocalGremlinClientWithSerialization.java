package com.tinkerpop.gremlin.client.executor;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;
import com.tinkerpop.gremlin.client.GremlinClient;

import javax.script.ScriptEngine;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.tinkerpop.gremlin.server.ResultCode;
import com.tinkerpop.gremlin.server.ResultSerializer;
import groovy.json.JsonSlurper;

/**
 * This class executes Gremlin on a local graph after performing serialization/deserialization of the bindings as well
 * as the serialization/deserialization of the outputs. It is useful for testing purposes since it follows the same
 * serialization and deserialization methods as the RemoteGremlinExecutor and the Gremlin Server.
 */
public class LocalGremlinClientWithSerialization extends RemoteGremlinClient implements GremlinClient {
    ScriptEngine scriptEngine;
    Map<String, Graph> graphs;
    JsonFactory factory;
    LocalGremlinClient evaluator;

    // A mock ObjectMapper mimicking the server
    ObjectMapper serverMapper;

    public LocalGremlinClientWithSerialization(Map<String, Graph> graphs, ScriptEngine scriptEngine, ObjectMapper mapper) {
        super(mapper);
        this.factory = new JsonFactory();
        this.evaluator = new LocalGremlinClient(graphs, scriptEngine);
        this.serverMapper = new ObjectMapper();
    }

    public String getStackTrace(Throwable t) {
        if (t == null) {
            return null;
        } else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return sw.toString();
        }
    }

    @Override
    protected String evalAtServer(String requestJson) {
        try {
            // De-serialize the request
            String graphName;
            String serverScript;
            Map<String, Object> serverGroovyBindings;

            try {
                JsonNode root = serverMapper.readTree(requestJson);
                graphName = root.get("g").asText();
                serverScript = root.get("s").asText();

                // Load the bindings
                Map<String, JsonNode> serverBindingJsons = new HashMap<String, JsonNode>();
                root.get("b").fields().forEachRemaining(binding -> serverBindingJsons.put(binding.getKey(), binding.getValue()));

                // Use JsonSlurper to parse the bindings
                serverGroovyBindings = new HashMap<String, Object>();

                for (Map.Entry<String, JsonNode> serverBindingJson : serverBindingJsons.entrySet()) {
                    StringWriter bindingJsonStr = new StringWriter();
                    serverMapper.writeTree(factory.createJsonGenerator(bindingJsonStr), serverBindingJson.getValue());
                    Object groovyValue = new JsonSlurper().parseText(bindingJsonStr.toString());
                    serverGroovyBindings.put(serverBindingJson.getKey(), groovyValue);
                }
            } catch (IOException e) {
                throw new GremlinClientException(GremlinClientErrorCodes.REQUEST_DESERIALIZATION_ERROR, "Unable to de-serialize request: " + requestJson, e);
            }

            // The Gremlin server will use the local executor
            Iterator<Object> result = evaluator.eval(graphName, serverGroovyBindings, serverScript);

            // Use the ResultSerializer to generate the result JSON
            return new ResultSerializer.JsonResultSerializer().serialize(result, ResultCode.SUCCESS, null);
        } catch (GremlinClientException e) {
            ResultCode failCode = ResultCode.FAIL;
            if (e.getErrorCode() == GremlinClientErrorCodes.REQUEST_DESERIALIZATION_ERROR) {
                failCode = ResultCode.FAIL_MALFORMED_REQUEST;
            }

            // TODO: Separate results, error details and stack trace
            String errorDetails = e.getMessage() + ". Stack trace: " + getStackTrace(e); // need to use this
            return new ResultSerializer.JsonResultSerializer().serialize(errorDetails, failCode, null);
        }
    }

//    private String serialize(Object item) throws JsonProcessingException {
//        if (item instanceof Holder) {
//            return serialize(((Holder) item).get());
//        } else if (item instanceof Vertex) {
//            return serialize(((Vertex) item).getId());
//        } else {
//            return mapper.writeValueAsString(item);
//        }
//    }
}
