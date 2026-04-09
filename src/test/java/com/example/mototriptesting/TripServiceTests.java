package com.example.mototriptesting;

import com.example.mototriptesting.repositories.TripRepository;
import com.example.mototriptesting.repositories.UserRepository;
import com.example.mototriptesting.services.TripService;
import com.example.mototriptesting.entities.Trip;
import com.example.mototriptesting.entities.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class TripServiceTests {
    @Mock
    private UserRepository userRepository;
    @Mock
    private TripRepository tripRepository;

    @InjectMocks
    private TripService tripService;

    @Test
    void shouldCreateTrip() {
        // Arrange
        Trip trip = new Trip("Voyage", 5, false);
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        // Act
        Trip result = tripService.createTrip("Voyage", 5, false);

        // Asserts
        assertNotNull(result);
        assertEquals(5, result.remainingPlaces());

        verify(tripRepository, times(1)).save(any(Trip.class));
    }

    @Test
    void shouldJoinTrip() {
        // Arrange
        Trip trip = new Trip("Voyage", 2, false);
        User user = new User("bob", false);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(tripRepository.save(any(Trip.class))).thenReturn(trip);

        // Act
        Trip result = tripService.joinTrip(1L, 2L);

        // Asserts
        assertNotNull(result);
        assertEquals(1, result.remainingPlaces());

        verify(tripRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).findById(2L);
        verify(tripRepository, times(1)).save(trip);
    }

    @Test
    void shouldNotFindUser() {
        // Arrange
        Trip trip = new Trip("voyage", 2, false);
        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(userRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tripService.joinTrip(1L, 2L)
        );
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void shouldNotFindTrip() {
        // Arrange
        when(tripRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Arrange
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> tripService.joinTrip(1L, 2L)
        );
        assertEquals("Trip not found", exception.getMessage());
    }
}
