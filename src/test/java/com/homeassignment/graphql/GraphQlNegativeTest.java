package com.homeassignment.graphql;

import com.homeassignment.config.ApiConfig;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@DisplayName("Hygraph GraphQL - Negative (Video schema)")
class GraphQlNegativeTest {

    private static final String NON_EXISTENT_MOVIE_ID = "clxxxxxxxxxxxxxxxxxxxxxxxxxx";

    private static RequestSpecification spec;

    @BeforeAll
    static void setUp() {
        ApiConfig.enableRelaxedHttps();
        spec = ApiConfig.graphQlSpec();
    }

    @Test
    @DisplayName("Invalid ID returns HTTP 200 with data.movie = null (no errors)")
    void shouldReturnNullDataForNonExistentId() {
        // Observed Hygraph behavior: HTTP 200 + data.movie = null (not errors[])
        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("""
                        query MovieById($id: ID!) {
                          movie(where: { id: $id }) {
                            id
                            title
                          }
                        }
                        """, Map.of("id", NON_EXISTENT_MOVIE_ID)).toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat((Object) response.jsonPath().get("data.movie")).isNull();
        assertThat(response.jsonPath().getList("errors")).isNull();
    }

    @Test
    @DisplayName("Malformed query returns errors[].message and data = null")
    void shouldReturnParseErrorForMalformedQuery() {
        // Observed Hygraph behavior: HTTP 400 for parse errors
        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("{ movies(first: 1 { id } }").toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat((Object) response.jsonPath().get("data")).isNull();
        assertThat(response.jsonPath().getList("errors")).isNotEmpty();
        assertThat(response.jsonPath().getString("errors[0].message"))
                .isNotBlank()
                .contains("ParseError");
    }

    @Test
    @DisplayName("Non-existent field returns a validation error")
    void shouldReturnValidationErrorForUnknownField() {
        // Observed Hygraph behavior: HTTP 400 + data = null + errors[]
        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("""
                        {
                          movies(first: 1) {
                            id
                            nonExistentField
                          }
                        }
                        """).toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(400);
        assertThat((Object) response.jsonPath().get("data")).isNull();
        assertThat(response.jsonPath().getList("errors")).isNotEmpty();
        assertThat(response.jsonPath().getString("errors[0].message"))
                .contains("nonExistentField")
                .contains("not defined");
    }
}
