package com.homeassignment.graphql;

import com.homeassignment.config.ApiConfig;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Epic("Hygraph GraphQL")
@Feature("Positive scenarios")
@DisplayName("Hygraph GraphQL - Positive (Video schema)")
class GraphQlPositiveTest {

    private static final int PAGE_LIMIT = 3;

    private static RequestSpecification spec;
    private static String existingMovieId;

    @BeforeAll
    static void setUp() {
        ApiConfig.enableRelaxedHttps();
        spec = ApiConfig.graphQlSpec();

        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("""
                        {
                          movies(first: 1) {
                            id
                          }
                        }
                        """).toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(200);
        existingMovieId = response.jsonPath().getString("data.movies[0].id");
        assertThat(existingMovieId).isNotBlank();
    }

    @Test
    @Story("Pagination / limit")
    @DisplayName("Query a list with pagination/limit")
    void shouldQueryMoviesWithLimit() {
        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("""
                        {
                          movies(first: %d) {
                            id
                            title
                          }
                        }
                        """.formatted(PAGE_LIMIT)).toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("errors")).isNull();

        List<Map<String, Object>> movies = response.jsonPath().getList("data.movies");
        assertThat(movies).isNotNull().hasSizeLessThanOrEqualTo(PAGE_LIMIT);
        assertThat(movies).allSatisfy(movie -> {
            assertThat(movie.get("id")).as("movie id").isNotNull();
            assertThat(String.valueOf(movie.get("title"))).as("movie title").isNotBlank();
        });
    }

    @Test
    @Story("Query by ID")
    @DisplayName("Query a single entity by ID")
    void shouldQuerySingleMovieById() {
        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("""
                        query MovieById($id: ID!) {
                          movie(where: { id: $id }) {
                            id
                            title
                          }
                        }
                        """, Map.of("id", existingMovieId)).toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("errors")).isNull();
        assertThat(response.jsonPath().getString("data.movie.id")).isEqualTo(existingMovieId);
        assertThat(response.jsonPath().getString("data.movie.title")).isNotBlank();
    }

    @Test
    @Story("GraphQL variables")
    @DisplayName("Query using GraphQL variables (not string interpolation)")
    void shouldQueryUsingVariables() {
        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("""
                        query MoviesPage($first: Int!) {
                          movies(first: $first) {
                            id
                            title
                          }
                        }
                        """, Map.of("first", PAGE_LIMIT)).toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("errors")).isNull();
        assertThat(response.jsonPath().getList("data.movies")).hasSizeLessThanOrEqualTo(PAGE_LIMIT);
        assertThat(response.jsonPath().getList("data.movies.id")).allSatisfy(id ->
                assertThat(String.valueOf(id)).isNotBlank());
    }

    @Test
    @Story("Fragment and nested fields")
    @DisplayName("Query with fragment and nested fields (movie → publishedBy → name)")
    void shouldQueryWithFragmentAndNestedFields() {
        Response response = given()
                .spec(spec)
                .body(GraphQlRequest.of("""
                        query MovieWithPublisher($id: ID!) {
                          movie(where: { id: $id }) {
                            ...MovieDetails
                          }
                        }

                        fragment MovieDetails on Movie {
                          id
                          title
                          publishedBy {
                            id
                            name
                          }
                        }
                        """, Map.of("id", existingMovieId)).toBody())
        .when()
                .post();

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getList("errors")).isNull();
        assertThat(response.jsonPath().getString("data.movie.id")).isEqualTo(existingMovieId);
        assertThat(response.jsonPath().getString("data.movie.title")).isNotBlank();
        assertThat(response.jsonPath().getString("data.movie.publishedBy.name")).isNotBlank();
    }
}
