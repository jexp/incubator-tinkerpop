package com.tinkerpop.gremlin.client;

/** Error codes used by the Gremlin Client */
public enum GremlinClientErrorCodes {
    INTERNAL_ERROR {
        public String toString() {
            return "INTERNAL_ERROR: An internal error occurred";
        }
    },

    SERVER_ERROR {
        public String toString() {
            return "SERVER_ERROR: An error occurred in the Gremlin server";
        }
    },

    SCRIPT_EXEC_ERROR {
        public String toString() {
            return "SCRIPT_EXEC_ERROR: An error occurred while executing the given script";
        }
    },

    REQUEST_SERIALIZATION_ERROR {
        public String toString() {
            return "REQUEST_SERIALIZATION_ERROR: A request to the Gremlin Server could not be serialized";
        }
    },

    REQUEST_DESERIALIZATION_ERROR {
        public String toString() {
            return "REQUEST_DESERIALIZATION_ERROR: The given request from a Gremlin client could not be de-serialized";
        }
    },

    RESPONSE_SERIALIZATION_ERROR {
        public String toString() {
            return "RESPONSE_SERIALIZATION_ERROR: A response from the Gremlin server could not be serialized";
        }
    },

    RESPONSE_DESERIALIZATION_ERROR {
        public String toString() {
            return "RESPONSE_DESERIALIZATION_ERROR: The given response from the Gremlin server could not be de-serialized";
        }
    },

    UNDEFINED_GRAPH {
        public String toString() {
            return "UNDEFINED_GRAPH: The graph addressed by the script is not defined in the Gremlin server";
        }
    },

    CONNECTION_ALREADY_CLOSED {
        public String toString() {
            return "CONNECTION_ALREADY_CLOSED: An operation was performed on a connection that was already closed";
        }
    },

    REMOTE_OBJECTS_ARE_IMMUTABLE {
        public String toString() {
            return "REMOTE_OBJECTS_ARE_IMMUTABLE: Objects representing remote vertices, edges, properties, etc. can not be changed through the Gremlin client";
        }
    },

    REMOTE_OBJECTS_CANT_BE_QUERIED {
        public String toString() {
            return "REMOTE_OBJECTS_CANT_BE_QUERIED: Objects representing remote vertices and edges can not be queried through the Gremlin client. You must include the query in the Gremlin expression";
        }
    }
}
