package com.homeassignment.restfulbooker;

import com.homeassignment.config.ApiConfig;
import com.homeassignment.restfulbooker.model.AuthRequest;
import com.homeassignment.restfulbooker.model.Booking;
import com.homeassignment.restfulbooker.model.BookingDates;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Tag("api")
@Epic("Restful Booker API")
@Feature("Booking CRUD")
@DisplayName("Restful Booker - Booking CRUD")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RestfulBookerCrudTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 1500L;

    private static RequestSpecification spec;
    private static String authToken;
    private static Integer bookingId;

    private static final Booking INITIAL_BOOKING = new Booking(
            "Alice",
            "Tester",
            150,
            true,
            new BookingDates("2026-08-01", "2026-08-05"),
            "Breakfast"
    );

    private static final Booking UPDATED_BOOKING = new Booking(
            "Alice",
            "Updated",
            220,
            false,
            new BookingDates("2026-09-10", "2026-09-15"),
            "Late checkout"
    );

    @BeforeAll
    static void setUp() {
        ApiConfig.enableRelaxedHttps();
        spec = ApiConfig.restfulBookerSpec();

        Response authResponse = given()
                .spec(spec)
                .body(ApiConfig.toJson(new AuthRequest(ApiConfig.AUTH_USERNAME, ApiConfig.AUTH_PASSWORD)))
        .when()
                .post("/auth");

        assertThat(authResponse.statusCode()).isEqualTo(200);
        authToken = authResponse.jsonPath().getString("token");
        assertThat(authToken).as("Auth token must be obtained before CRUD tests").isNotBlank();
    }

    @Test
    @Order(1)
    @Story("Create")
    @DisplayName("Create a new booking (POST /booking)")
    void createBooking() {
        Response response = postWithRetry("/booking", ApiConfig.toJson(INITIAL_BOOKING));

        assertThat(response.statusCode())
                .as("Create booking failed. Body: %s", response.asString())
                .isEqualTo(200);

        bookingId = response.jsonPath().getInt("bookingid");
        assertThat(bookingId).isPositive();
        assertThat(response.jsonPath().getString("booking.firstname")).isEqualTo(INITIAL_BOOKING.getFirstname());
        assertThat(response.jsonPath().getString("booking.lastname")).isEqualTo(INITIAL_BOOKING.getLastname());
        assertThat(response.jsonPath().getInt("booking.totalprice")).isEqualTo(INITIAL_BOOKING.getTotalprice());
        assertThat(response.jsonPath().getBoolean("booking.depositpaid")).isEqualTo(INITIAL_BOOKING.isDepositpaid());
        assertThat(response.jsonPath().getString("booking.bookingdates.checkin"))
                .isEqualTo(INITIAL_BOOKING.getBookingdates().getCheckin());
        assertThat(response.jsonPath().getString("booking.bookingdates.checkout"))
                .isEqualTo(INITIAL_BOOKING.getBookingdates().getCheckout());
        assertThat(response.jsonPath().getString("booking.additionalneeds"))
                .isEqualTo(INITIAL_BOOKING.getAdditionalneeds());
    }

    @Test
    @Order(2)
    @Story("Read")
    @DisplayName("Retrieve the booking by ID (GET /booking/{id})")
    void retrieveBookingById() {
        assumeTrue(bookingId != null && bookingId > 0, "Booking was not created");

        Response response = given()
                .spec(spec)
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}");

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.contentType()).contains("application/json");
        assertThat(response.jsonPath().getString("firstname")).isEqualTo(INITIAL_BOOKING.getFirstname());
        assertThat(response.jsonPath().getString("lastname")).isEqualTo(INITIAL_BOOKING.getLastname());
        assertThat(response.jsonPath().getInt("totalprice")).isEqualTo(INITIAL_BOOKING.getTotalprice());
        assertThat(response.jsonPath().getBoolean("depositpaid")).isEqualTo(INITIAL_BOOKING.isDepositpaid());
        assertThat(response.jsonPath().getString("bookingdates.checkin"))
                .isEqualTo(INITIAL_BOOKING.getBookingdates().getCheckin());
        assertThat(response.jsonPath().getString("bookingdates.checkout"))
                .isEqualTo(INITIAL_BOOKING.getBookingdates().getCheckout());
        assertThat(response.jsonPath().getString("additionalneeds"))
                .isEqualTo(INITIAL_BOOKING.getAdditionalneeds());
    }

    @Test
    @Order(3)
    @Story("Update")
    @DisplayName("Update the booking (PUT /booking/{id})")
    void updateBooking() {
        assumeTrue(bookingId != null && bookingId > 0, "Booking was not created");

        Response response = putWithRetry("/booking/" + bookingId, ApiConfig.toJson(UPDATED_BOOKING));

        assertThat(response.statusCode())
                .as("Update booking failed. Body: %s", response.asString())
                .isEqualTo(200);
        assertThat(response.jsonPath().getString("firstname")).isEqualTo(UPDATED_BOOKING.getFirstname());
        assertThat(response.jsonPath().getString("lastname")).isEqualTo(UPDATED_BOOKING.getLastname());
        assertThat(response.jsonPath().getInt("totalprice")).isEqualTo(UPDATED_BOOKING.getTotalprice());
        assertThat(response.jsonPath().getBoolean("depositpaid")).isEqualTo(UPDATED_BOOKING.isDepositpaid());
        assertThat(response.jsonPath().getString("bookingdates.checkin"))
                .isEqualTo(UPDATED_BOOKING.getBookingdates().getCheckin());
        assertThat(response.jsonPath().getString("bookingdates.checkout"))
                .isEqualTo(UPDATED_BOOKING.getBookingdates().getCheckout());
        assertThat(response.jsonPath().getString("additionalneeds"))
                .isEqualTo(UPDATED_BOOKING.getAdditionalneeds());
    }

    @Test
    @Order(4)
    @Story("Delete")
    @DisplayName("Delete the booking (DELETE /booking/{id})")
    void deleteBooking() {
        assumeTrue(bookingId != null && bookingId > 0, "Booking was not created");

        Response deleteResponse = deleteWithRetry("/booking/" + bookingId);
        assertThat(deleteResponse.statusCode())
                .as("Delete booking failed. Body: %s", deleteResponse.asString())
                .isEqualTo(201);

        Response getAfterDelete = given()
                .spec(spec)
                .pathParam("id", bookingId)
        .when()
                .get("/booking/{id}");

        assertThat(getAfterDelete.statusCode()).isEqualTo(404);
    }

    private static Response postWithRetry(String path, String body) {
        Response last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            last = given().spec(spec).body(body).when().post(path);
            if (last.statusCode() != 418) {
                return last;
            }
            sleepQuietly(RETRY_DELAY_MS * attempt);
        }
        assertThat(last == null ? -1 : last.statusCode())
                .as("POST %s kept returning 418. Last body: %s", path, last == null ? "n/a" : last.asString())
                .isNotEqualTo(418);
        return last;
    }

    private static Response putWithRetry(String path, String body) {
        Response last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            last = given()
                    .spec(spec)
                    .header("Cookie", "token=" + authToken)
                    .body(body)
            .when()
                    .put(path);
            if (last.statusCode() != 418) {
                return last;
            }
            sleepQuietly(RETRY_DELAY_MS * attempt);
        }
        assertThat(last == null ? -1 : last.statusCode())
                .as("PUT %s kept returning 418. Last body: %s", path, last == null ? "n/a" : last.asString())
                .isNotEqualTo(418);
        return last;
    }

    private static Response deleteWithRetry(String path) {
        Response last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            last = given()
                    .spec(spec)
                    .header("Cookie", "token=" + authToken)
            .when()
                    .delete(path);
            if (last.statusCode() != 418) {
                return last;
            }
            sleepQuietly(RETRY_DELAY_MS * attempt);
        }
        assertThat(last == null ? -1 : last.statusCode())
                .as("DELETE %s kept returning 418. Last body: %s", path, last == null ? "n/a" : last.asString())
                .isNotEqualTo(418);
        return last;
    }

    private static void sleepQuietly(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
