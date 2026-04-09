package com.example.mototriptesting.unitTests;

import com.example.mototriptesting.entities.User;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class UserTests {
    
    @Test
    public void AddPointsTest() throws Exception {
        // Arrange
        User user = new User("yanis", false);

        // Act
        user.addPoints(10);

        // Assert
        Field field = User.class.getDeclaredField("points");
        field.setAccessible(true);
        int result = (int)field.get(user);

        assertEquals(10, result);
    }

    @Test
    public void CanJoinWithNoPremiumTest() {
        // Arrange
        User user = new User("yanis", false);

        // Act
        boolean result = user.canJoinPremium();

        // Assert
        assertEquals(false, result);
    }

    @Test
    public void CanJoinWithPremiumTest() {
        // Arrange
        User user = new User("yanis", true);

        // Act
        boolean result = user.canJoinPremium();

        // Assert
        assertEquals(true, result);
    }
}
