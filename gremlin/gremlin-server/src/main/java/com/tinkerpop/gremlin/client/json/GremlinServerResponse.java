package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

import java.util.ArrayList;

/**
 * NOT USED -- This class is a data holder for responses from the gremlin server.
 */
public class GremlinServerResponse {
    ArrayList<String> output;
    ExceptionResponse exception;

    @JsonCreator
    public GremlinServerResponse(@JsonProperty("out") ArrayList<String> output,
                                 @JsonProperty("exception") ExceptionResponse exception) {
        this.output = output;
        this.exception = exception;
    }

    public GremlinServerResponse(ArrayList<String> output) {
        this.output = output;
    }

    public GremlinServerResponse(ExceptionResponse exception) {
        this.exception = exception;
    }

    @JsonIgnore
    public ArrayList<String> value() throws GremlinClientException {
        if (exception != null) {
            throw exception.exception();
        } else {
            return output;
        }
    }

    @JsonProperty("out")
    public ArrayList<String> getOutput() {
        return output;
    }

    @JsonProperty("exception")
    public ExceptionResponse getException() {
        return exception;
    }
}