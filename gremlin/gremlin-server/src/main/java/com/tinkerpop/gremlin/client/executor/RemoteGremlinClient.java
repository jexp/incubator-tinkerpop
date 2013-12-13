package com.tinkerpop.gremlin.client.executor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;
import com.tinkerpop.gremlin.client.GremlinClient;
import com.tinkerpop.gremlin.client.json.GremlinServerRequest;
import com.tinkerpop.gremlin.client.json.RemoteThing;
import com.tinkerpop.gremlin.server.ResultCode;
import com.tinkerpop.gremlin.server.ResultSerializer;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

/** This abstract class performs the serialization of requests, and deserialization of the response */
public abstract class RemoteGremlinClient implements GremlinClient {
    private ObjectMapper mapper;

    public RemoteGremlinClient(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    abstract String evalAtServer(String requestJson) throws GremlinClientException;

    @Override
    public Iterator<Object> eval(String graphName, Map<String, Object> bindingObjs, String script, Class itemClass) throws RuntimeException {
        final Iterator<JsonNode> result = evalJson(graphName, bindingObjs, script);

        return new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return result.hasNext();
            }

            @Override
            public Object next() {
                JsonNode item = result.next();
                try {
                    return mapper.readValue(item.traverse(), itemClass);
                } catch (IOException e) {
                    throw new GremlinClientException(GremlinClientErrorCodes.RESPONSE_SERIALIZATION_ERROR, "Unable to de-serialize " + item, e);
                }
            }
        };
    }

    @Override
    public Iterator<Object> eval(String graphName, Map<String, Object> bindingObjs, String script, TypeReference itemType) throws RuntimeException {
        final Iterator<JsonNode> result = evalJson(graphName, bindingObjs, script);

        return new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return result.hasNext();
            }

            @Override
            public Object next() {
                JsonNode item = result.next();
                try {
                    return mapper.readValue(item.traverse(), itemType);
                } catch (IOException e) {
                    throw new GremlinClientException(GremlinClientErrorCodes.RESPONSE_SERIALIZATION_ERROR, "Unable to de-serialize " + item, e);
                }
            }
        };
    }

    @Override
    public Iterator<Object> eval(String graphName, Map<String, Object> bindingObjs, String script) throws RuntimeException {
        // TODO: The current default de-serialization assumes that the result is a list of vertex/edge objects
        // Is this OK? Or should we implement a more generic deserialization routine that handles simple types?
        return eval(graphName, bindingObjs, script, RemoteThing.class);
    }

    /** This method is the same as eval() except the response is an Iterator of JsonNode's whose items must be de-serialized based on the caller's hints */
    protected Iterator<JsonNode> evalJson(String graphName, Map<String, Object> bindingObjs, String script) throws RuntimeException {
        // Prepare the request
        String requestJson = serializeRequest(graphName, bindingObjs, script);

        // And send it over the wire... (not done here)
        System.out.println("Sending: " + requestJson);

        String responseJson = evalAtServer(requestJson);

        // Receive the response
        System.out.println("Received: " + responseJson);

        // And de-serialize it to an iterator of JsonNode's
        return deserializeResponse(responseJson);
    }

    private String serializeRequest(String graphName, Map<String, Object> bindingObjs, String script) throws GremlinClientException {
        try {
            // The Gremlin client will prepare the request
            GremlinServerRequest request = new GremlinServerRequest(graphName, script, bindingObjs);

            // Then serialize it
            return mapper.writeValueAsString(request);
        } catch (JsonProcessingException e) {
            throw new GremlinClientException(GremlinClientErrorCodes.REQUEST_SERIALIZATION_ERROR, "Unable to serialize request", e);
        }
    }

    private Iterator<JsonNode> deserializeResponse(String responseJson) throws GremlinClientException {
        try {
            // The client deserializes the results
            JsonNode responseRoot = mapper.readTree(responseJson);
            int code = responseRoot.get(ResultSerializer.JsonResultSerializer.TOKEN_CODE).asInt();
            if (code != ResultCode.SUCCESS.getValue()) {
                String errorDetails = responseRoot.get(ResultSerializer.JsonResultSerializer.TOKEN_RESULT).asText();
                throw new GremlinClientException(GremlinClientErrorCodes.SERVER_ERROR, errorDetails);
            }

            JsonNode responseResult = responseRoot.get(ResultSerializer.JsonResultSerializer.TOKEN_RESULT);
            if (responseResult.isNull()) {
                return null;
            } else if (!responseResult.isArray()) {
                throw new GremlinClientException(GremlinClientErrorCodes.RESPONSE_DESERIALIZATION_ERROR, "Expecting the result to be a list, but encountered a non-list in: " + responseJson);
            } else {
                return responseResult.elements();
            }
        } catch (GremlinClientException e) {
            throw e;
        } catch (IOException e) {
            throw new GremlinClientException(GremlinClientErrorCodes.RESPONSE_DESERIALIZATION_ERROR, "Unable to de-serialize the response: " + responseJson, e);
        }
    }
}
