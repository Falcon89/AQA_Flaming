package com.homeassignment.restfulbooker;

import com.homeassignment.config.ApiConfig;
import com.homeassignment.restfulbooker.model.AuthRequest;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Epic("Restful Booker API")
@Feature("Authentication")
@DisplayName("Restful Booker - Authentication")
class RestfulBookerAuthTest {

    private static RequestSpecification spec;

    @BeforeAll
    static void setUp() {
        ApiConfig.enableRelaxedHttps();
        spec = ApiConfig.restfulBookerSpec();
    }

    @Test
    @Story("Valid credentials")
    @DisplayName("POST /auth returns a non-empty token for valid credentials")
    void shouldAuthenticateAndReturnToken() {
        Response response = given()
                .spec(spec)
                .body(ApiConfig.toJson(new AuthRequest(ApiConfig.AUTH_USERNAME, ApiConfig.AUTH_PASSWORD)))
        .when()
                .post("/auth");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("token"))
                .as("auth token")
                .isNotBlank();
    }

    @Test
    @Story("Invalid credentials")
    @DisplayName("POST /auth fails for invalid credentials")
    void shouldRejectInvalidCredentials() {
        Response response = given()
                .spec(spec)
                .body(ApiConfig.toJson(new AuthRequest("admin", "wrong-password")))
        .when()
                .post("/auth");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getString("reason")).isEqualTo("Bad credentials");
    }
}
