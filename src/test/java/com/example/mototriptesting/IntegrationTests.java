package com.example.mototriptesting;

import com.example.mototriptesting.entities.Trip;
import com.example.mototriptesting.entities.User;
import com.example.mototriptesting.repositories.TripRepository;
import com.example.mototriptesting.repositories.UserRepository;
import com.example.mototriptesting.services.TripService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Transactional // rollback automatique après chaque test
class IntegrationTests {

    @Autowired
    private TripService tripService;

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldPersistAndRetrieveUser() {
        // Arrange
        User user = tripService.createUser("Alice", true);
        Long userId = (Long) ReflectionTestUtils.getField(user, "id");

        // Act
        User retrieved = userRepository.findById(userId).orElse(null);

        // Assert
        assertNotNull(userId);
        assertNotNull(retrieved);
    }

    @Test
    void shouldPersistAndRetrieveTrip() {
        // Arrange
        Trip trip = tripService.createTrip("Voyage Corse", 5, false);
        Long tripId = (Long) ReflectionTestUtils.getField(trip, "id");

        // Act
        Trip retrieved = tripRepository.findById(tripId).orElse(null);

        // Assert
        assertNotNull(tripId);
        assertNotNull(retrieved);
        assertEquals(5, retrieved.remainingPlaces());
    }

    @Test
    void shouldManageUserTripRelations() {
        // Arrange
        User user = tripService.createUser("Bob", false);
        Trip trip = tripService.createTrip("Voyage Sud", 3, false);
        Long userId = (Long) ReflectionTestUtils.getField(user, "id");
        Long tripId = (Long) ReflectionTestUtils.getField(trip, "id");

        // Act
        Trip updatedTrip = tripService.joinTrip(tripId, userId);

        // Assert
        assertEquals(2, updatedTrip.remainingPlaces(), "La place a bien été décomptée en base");
        User updatedUser = userRepository.findById(userId).orElseThrow();
        int pointsEnBase = (int) ReflectionTestUtils.getField(updatedUser, "points");
        assertEquals(10, pointsEnBase, "L'utilisateur a reçu 10 points persistés en base");
    }

    @Test
    void shouldReturnAllTrips() {
        // Arrange
        tripService.createTrip("Voyage 1", 2, false);
        tripService.createTrip("Voyage 2", 4, false);

        // Act
        List<Trip> trips = tripService.allTrips();

        // Assert
        assertTrue(trips.size() >= 2);
    }
}
