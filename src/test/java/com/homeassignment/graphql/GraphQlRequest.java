package com.homeassignment.graphql;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds GraphQL POST bodies with a query and optional variables map.
 */
final class GraphQlRequest {

    private final String query;
    private final Map<String, Object> variables;

    private GraphQlRequest(String query, Map<String, Object> variables) {
        this.query = query;
        this.variables = variables;
    }

    static GraphQlRequest of(String query) {
        return new GraphQlRequest(query, null);
    }

    static GraphQlRequest of(String query, Map<String, Object> variables) {
        return new GraphQlRequest(query, variables);
    }

    Map<String, Object> toBody() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("query", query);
        if (variables != null) {
            body.put("variables", variables);
        }
        return body;
    }
}
