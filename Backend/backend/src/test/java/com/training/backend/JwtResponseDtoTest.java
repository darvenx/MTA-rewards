package com.training.backend;
import com.training.dto.JwtResponseDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JwtResponseDtoTest {

    @Test
    void testSetAndGetToken() {
        JwtResponseDto dto = new JwtResponseDto();
        dto.setToken("sampleToken123");

        assertEquals("sampleToken123", dto.getToken(),
                "The getter should return the same token that was set");
    }

    @Test
    void testDefaultTokenIsNull() {
        JwtResponseDto dto = new JwtResponseDto();

        assertNull(dto.getToken(),
                "By default, token should be null before setting any value");
    }

    @Test
    void testTokenCanBeUpdated() {
        JwtResponseDto dto = new JwtResponseDto();
        dto.setToken("firstToken");
        dto.setToken("updatedToken");

        assertEquals("updatedToken", dto.getToken(),
                "The token should reflect the most recent value set");
    }

    @Test
    void testTokenWithEmptyString() {
        JwtResponseDto dto = new JwtResponseDto();
        dto.setToken("");

        assertEquals("", dto.getToken(),
                "The token should allow empty string values");
    }
}
