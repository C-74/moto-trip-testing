package com.example.mototriptesting.unitTests;

import com.example.mototriptesting.entities.Trip;
import com.example.mototriptesting.entities.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TripTests {

    @Test
    public void JoinTest() {
        // Arrange
        Trip trip = new Trip("Voyage 1", 2, false);
        User user = new User("yanis", false);

        // Act
        trip.join(user);

        // Assert
        assertEquals(1, trip.remainingPlaces());
    }

    @Test
    public void CantJoinFullTest() {
        // Arrange
        Trip trip = new Trip("Voyage 1", 1, false);
        User user = new User("yanis", false);
        User user2 = new User("bob", false);

        // Act
        trip.join(user);

        // Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trip.join(user2)
        );
        assertEquals("Trip full", exception.getMessage());
    }

    @Test
    public void StartTripTest() {
        // Arrange
        Trip trip = new Trip("Voyage 2", 2, false);
        User user = new User("yanis", false);
        trip.join(user);

        // Act
        trip.start();

        // Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trip.join(new User("retard", false))
        );
        assertEquals("Trip already started", exception.getMessage());
    }

    @Test
    public void CantStartEmptyTripTest() {
        // Arrange
        Trip trip = new Trip("Voyage Vide", 2, false);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trip.start()
        );
        assertEquals("No participants", exception.getMessage());
    }

    @Test
    public void CantJoinPremiumTest() {
        // Arrange
        Trip trip = new Trip("Voyage Premium", 2, true);
        User user = new User("yanis", false);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> trip.join(user)
        );
        assertEquals("Premium required", exception.getMessage());
    }
}
