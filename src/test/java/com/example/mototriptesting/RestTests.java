package com.example.mototriptesting;

import com.example.mototriptesting.controllers.TripController;
import com.example.mototriptesting.entities.Trip;
import com.example.mototriptesting.entities.User;
import com.example.mototriptesting.services.TripService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.TestPropertySource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TripController.class)
@TestPropertySource(properties = "spring.jackson.visibility.field=any")
public class RestTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TripService tripService; // On simule le service pour isoler le controller

    @Test
    void shouldCreateUser() throws Exception {
        // Arrange
        User mockUser = new User("Alice", true);
        org.springframework.test.util.ReflectionTestUtils.setField(mockUser, "id", 1L);

        given(tripService.createUser("Alice", true)).willReturn(mockUser);

        Map<String, Object> requestBody = Map.of(
                "name", "Alice",
                "premium", true
        );

        // Act & Assert
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Alice"));
    }

    @Test
    void shouldCreateTrip() throws Exception {
        // Arrange
        Trip mockTrip = new Trip("Voyage Corse", 5, false);
        org.springframework.test.util.ReflectionTestUtils.setField(mockTrip, "id", 1L);

        given(tripService.createTrip("Voyage Corse", 5, false)).willReturn(mockTrip);

        Map<String, Object> requestBody = Map.of(
                "name", "Voyage Corse",
                "maxParticipants", 5,
                "premiumOnly", false
        );

        // Act & Assert
        mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.maxParticipants").value(5));
    }

    @Test
    void shouldJoinTrip() throws Exception {
        // Arrange
        Trip mockTrip = new Trip("Voyage Corse", 5, false);
        org.springframework.test.util.ReflectionTestUtils.setField(mockTrip, "id", 1L);
        User user = new User("Alice", false);
        mockTrip.join(user); // Fait baisser de 1 les places

        given(tripService.joinTrip(1L, 2L)).willReturn(mockTrip);

        // Act & Assert
        mockMvc.perform(post("/api/trips/1/join")
                        .param("userId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants.length()").value(1))
                .andExpect(jsonPath("$.participants[0].name").value("Alice"));
    }

    @Test
    void shouldStartTrip() throws Exception {
        // Arrange
        Trip mockTrip = new Trip("Voyage Corse", 5, false);
        org.springframework.test.util.ReflectionTestUtils.setField(mockTrip, "id", 1L);
        User user = new User("Alice", false);
        mockTrip.join(user);
        mockTrip.start();

        given(tripService.startTrip(1L)).willReturn(mockTrip);

        // Act & Assert
        mockMvc.perform(post("/api/trips/1/start"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.started").value(true));
    }

    @Test
    void shouldReturnAllTrips() throws Exception {
        // Arrange
        Trip trip1 = new Trip("Voyage 1", 2, false);
        Trip trip2 = new Trip("Voyage 2", 4, false);
        List<Trip> trips = Arrays.asList(trip1, trip2);

        given(tripService.allTrips()).willReturn(trips);

        // Act & Assert
        mockMvc.perform(get("/api/trips"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].maxParticipants").value(2))
                .andExpect(jsonPath("$[1].maxParticipants").value(4));
    }

    @Test
    void shouldHandleTripFullError() throws Exception {
        // Arrange
        given(tripService.joinTrip(1L, 2L)).willThrow(new RuntimeException("Trip full"));

        // Act & Assert
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> mockMvc.perform(post("/api/trips/1/join").param("userId", "2"))
        );
        assertTrue(exception.getCause() instanceof RuntimeException);
        assertEquals("Trip full", exception.getCause().getMessage());
    }

    @Test
    void shouldHandlePremiumRefusedError() throws Exception {
        // Arrange
        given(tripService.joinTrip(1L, 2L)).willThrow(new RuntimeException("Premium required"));

        // Act & Assert
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> mockMvc.perform(post("/api/trips/1/join").param("userId", "2"))
        );
        assertEquals("Premium required", exception.getCause().getMessage());
    }

    @Test
    void shouldHandleTripAlreadyStartedError() throws Exception {
        // Arrange
        given(tripService.joinTrip(1L, 2L)).willThrow(new RuntimeException("Trip already started"));

        // Act & Assert
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> mockMvc.perform(post("/api/trips/1/join").param("userId", "2"))
        );
        assertEquals("Trip already started", exception.getCause().getMessage());
    }

    @Test
    void shouldHandleUserNotFoundError() throws Exception {
        // Arrange
        given(tripService.joinTrip(1L, 2L)).willThrow(new RuntimeException("User not found"));

        // Act & Assert
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> mockMvc.perform(post("/api/trips/1/join").param("userId", "2"))
        );
        assertEquals("User not found", exception.getCause().getMessage());
    }

    @Test
    void shouldHandleTripNotFoundError() throws Exception {
        // Arrange
        given(tripService.joinTrip(1L, 2L)).willThrow(new RuntimeException("Trip not found"));

        // Act & Assert
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> mockMvc.perform(post("/api/trips/1/join").param("userId", "2"))
        );
        assertEquals("Trip not found", exception.getCause().getMessage());
    }

    @Test
    void shouldHandleInvalidCapacityError() throws Exception {
        // Arrange
        given(tripService.createTrip("Voyage Nul", 0, false))
                .willThrow(new IllegalArgumentException("Invalid capacity"));
        
        Map<String, Object> requestBody = Map.of(
                "name", "Voyage Nul",
                "maxParticipants", 0,
                "premiumOnly", false
        );

        // Act & Assert
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> mockMvc.perform(post("/api/trips")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
        );
        assertEquals("Invalid capacity", exception.getCause().getMessage());
    }
}
