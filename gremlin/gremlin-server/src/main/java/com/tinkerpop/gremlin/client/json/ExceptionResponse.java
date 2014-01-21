package com.tinkerpop.gremlin.client.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.tinkerpop.gremlin.client.GremlinClientErrorCodes;
import com.tinkerpop.gremlin.client.GremlinClientException;

/** NOT USED -- This class is a data holder for the exception object in a Gremlin server response */
public class ExceptionResponse {
    String code;
    String details;
    String stackTrace;

    @JsonCreator
    public ExceptionResponse(String code, String details, String stackTrace) {
        this.code = code;
        this.details = details;
        this.stackTrace = stackTrace;
    }

    @JsonIgnore
    public GremlinClientException exception() {
        GremlinClientErrorCodes errorCode = GremlinClientErrorCodes.SERVER_ERROR;
        try {
            errorCode = GremlinClientErrorCodes.valueOf(code);
        } catch (IllegalArgumentException e) {
            // ignore
        }

        throw new GremlinClientException(errorCode, details, stackTrace);
    }

    public String getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    public String getStackTrace() {
        return stackTrace;
    }
}
