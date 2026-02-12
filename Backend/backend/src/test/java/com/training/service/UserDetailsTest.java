package com.training.service;

import com.training.entities.User;
import com.training.enums.UserRole;
import com.training.repo.UserRepo;
<<<<<<< HEAD
import com.training.service.impl.UserDetailsImpl;
=======
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserDetailsTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
<<<<<<< HEAD
    private UserDetailsImpl userDetails;
=======
    private UserDetails userDetails;
>>>>>>> c2bcfbfa6018b37f88a52eef71d69fefd0f1cf24

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("password");
        testUser.setRole(UserRole.USER);
    }

    @Test
    void testLoadUserByUsername_UserFound() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        org.springframework.security.core.userdetails.UserDetails result = userDetails.loadUserByUsername("testuser");

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        assertEquals("password", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void testLoadUserByUsername_UserNotFound() {
        when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetails.loadUserByUsername("testuser"));
    }
}
