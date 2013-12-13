package com.tinkerpop.gremlin.client;

/**
 * An exception in the GremlinBuilder
 */
public class GremlinClientException extends RuntimeException {
    GremlinClientErrorCodes code;
    String serverStackTrace;

    public GremlinClientException(GremlinClientErrorCodes code) {
        super(code.toString());
        this.code = code;
    }

    public GremlinClientException(GremlinClientErrorCodes code, String details) {
        super(code.toString() + ". " + details);
        this.code = code;
    }

    public GremlinClientException(GremlinClientErrorCodes code, String details, Throwable t) {
        super(code.toString() + ". " + details, t);
        this.code = code;
    }

    public GremlinClientException(GremlinClientErrorCodes code, String details, String serverStackTrace) {
        super(code.toString() + ". " + details);
        this.code = code;
        this.serverStackTrace = serverStackTrace;
    }

    public GremlinClientErrorCodes getErrorCode() {
        return code;
    }

    public String getServerStackTrace() {
        return serverStackTrace;
    }
}
