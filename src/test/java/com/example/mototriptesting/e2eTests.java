package com.example.mototriptesting;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.jackson.visibility.field=any",
    "spring.datasource.url=jdbc:h2:mem:testdb;NON_KEYWORDS=USER"
})
public class e2eTests {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void e2eMotoTripScenario() {
        // 1️⃣ Créer un utilisateur
        Map<String, Object> userPayload = Map.of("name", "Laura", "premium", true);

        Number userId = given()
                .contentType(ContentType.JSON)
                .body(userPayload)
                .when()
                .post("/api/users")
                .then()
                .statusCode(200)
                .body("name", equalTo("Laura"))
                .extract().path("id");

        // 2️⃣ Créer un voyage
        Map<String, Object> tripPayload = Map.of(
                "name", "Roadtrip Alpes",
                "maxParticipants", 4,
                "premiumOnly", false
        );

        Number tripId = given()
                .contentType(ContentType.JSON)
                .body(tripPayload)
                .when()
                .post("/api/trips")
                .then()
                .statusCode(200)
                .body("name", equalTo("Roadtrip Alpes"))
                .extract().path("id");

        // 3️⃣ L'utilisateur rejoint le voyage
        given()
                .queryParam("userId", userId)
                .when()
                .post("/api/trips/{id}/join", tripId)
                .then()
                .statusCode(200)
                .body("participants.size()", equalTo(1))
                .body("participants[0].name", equalTo("Laura"));

        // 4️⃣ Démarrer le voyage
        given()
                .when()
                .post("/api/trips/{id}/start", tripId)
                .then()
                .statusCode(200)
                .body("started", equalTo(true));

        // 5️⃣ Vérifier état via GET /api/trips
        when()
                .get("/api/trips")
                .then()
                .statusCode(200)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.id == " + tripId + " }.started", equalTo(true))
                .body("find { it.id == " + tripId + " }.participants.size()", equalTo(1))
                .body("find { it.id == " + tripId + " }.participants[0].name", equalTo("Laura"));
    }
}
