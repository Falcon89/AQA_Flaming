package com.homeassignment.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public final class ApiConfig {

    public static final String RESTFUL_BOOKER_BASE_URI = "https://restful-booker.herokuapp.com";

    /**
     * Hygraph public Video Streaming demo schema from
     * https://hygraph.com/graphql-playground
     */
    public static final String GRAPHQL_ENDPOINT =
            "https://us-east-1-shared-usea1-02.cdn.hygraph.com/content/clpvcopq3aavs01usft1idkgj/master";

    public static final String AUTH_USERNAME = "admin";
    public static final String AUTH_PASSWORD = "password123";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private ApiConfig() {
    }

    public static RequestSpecification restfulBookerSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(RESTFUL_BOOKER_BASE_URI)
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "HomeAssignment-API-Tests/1.0")
                .addFilter(new AllureRestAssured())
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }

    public static RequestSpecification graphQlSpec() {
        return new RequestSpecBuilder()
                .setBaseUri(GRAPHQL_ENDPOINT)
                .setContentType(ContentType.JSON)
                .addHeader("Accept", "application/json")
                .addFilter(new AllureRestAssured())
                .log(LogDetail.METHOD)
                .log(LogDetail.URI)
                .build();
    }

    public static void enableRelaxedHttps() {
        RestAssured.useRelaxedHTTPSValidation();
    }

    public static String toJson(Object value) {
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize request body", e);
        }
    }
}
