package dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class UserDTOTest {

    @Test
    void testConstructorAndGetters() {
        Long id = 1L;
        String username = "testuser";
        String passwordHash = "hashedpassword123";
        String email = "test@example.com";
        String role = "USER";
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 1, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 2, 15, 30);

        UserDTO userDTO = new UserDTO(id, username, passwordHash, email, role, createdAt, updatedAt);

        assertEquals(id, userDTO.getId());
        assertEquals(username, userDTO.getUsername());
        assertEquals(passwordHash, userDTO.getPasswordHash());
        assertEquals(email, userDTO.getEmail());
        assertEquals(role, userDTO.getRole());
        assertEquals(createdAt, userDTO.getCreatedAt());
        assertEquals(updatedAt, userDTO.getUpdatedAt());
    }

    @Test
    void testSetters() {
        UserDTO userDTO = new UserDTO(1L, "olduser", "oldhash", "old@example.com", "ADMIN",
                LocalDateTime.now(), LocalDateTime.now());

        userDTO.setId(2L);
        userDTO.setUsername("newuser");
        userDTO.setPasswordHash("newhash");
        userDTO.setEmail("new@example.com");
        userDTO.setRole("MODERATOR");
        LocalDateTime newCreatedAt = LocalDateTime.of(2024, 2, 1, 12, 0);
        LocalDateTime newUpdatedAt = LocalDateTime.of(2024, 2, 2, 18, 0);
        userDTO.setCreatedAt(newCreatedAt);
        userDTO.setUpdatedAt(newUpdatedAt);

        assertEquals(2L, userDTO.getId());
        assertEquals("newuser", userDTO.getUsername());
        assertEquals("newhash", userDTO.getPasswordHash());
        assertEquals("new@example.com", userDTO.getEmail());
        assertEquals("MODERATOR", userDTO.getRole());
        assertEquals(newCreatedAt, userDTO.getCreatedAt());
        assertEquals(newUpdatedAt, userDTO.getUpdatedAt());
    }

    @Test
    void testToString() {
        Long id = 123L;
        String username = "johndoe";
        String passwordHash = "abc123hash";
        String email = "john@example.com";
        String role = "USER";
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 15, 9, 30);
        LocalDateTime updatedAt = LocalDateTime.of(2024, 1, 20, 14, 45);

        UserDTO userDTO = new UserDTO(id, username, passwordHash, email, role, createdAt, updatedAt);

        String result = userDTO.toString();

        assertNotNull(result);
        assertTrue(result.contains("UserDTO{"));
        assertTrue(result.contains("id=" + id));
        assertTrue(result.contains("email='" + email + "'"));
        assertTrue(result.contains("username='" + username + "'"));
        assertTrue(result.contains("passwordHash='" + passwordHash + "'"));
        assertTrue(result.contains("createdAt=" + createdAt));
        assertTrue(result.contains("role='" + role + "'"));
    }

    @Test
    void testToStringWithNullValues() {
        UserDTO userDTO = new UserDTO(null, null, null, null, null, null, null);

        String result = userDTO.toString();

        assertNotNull(result);
        assertTrue(result.contains("UserDTO{"));
        assertTrue(result.contains("id=null"));
        assertTrue(result.contains("email='null'"));
        assertTrue(result.contains("username='null'"));
        assertTrue(result.contains("passwordHash='null'"));
        assertTrue(result.contains("createdAt=null"));
        assertTrue(result.contains("role='null'"));
    }

    @Test
    void testSetterWithNullValues() {
        UserDTO userDTO = new UserDTO(1L, "user", "hash", "email@test.com", "USER",
                LocalDateTime.now(), LocalDateTime.now());

        userDTO.setId(null);
        userDTO.setUsername(null);
        userDTO.setPasswordHash(null);
        userDTO.setEmail(null);
        userDTO.setRole(null);
        userDTO.setCreatedAt(null);
        userDTO.setUpdatedAt(null);

        assertNull(userDTO.getId());
        assertNull(userDTO.getUsername());
        assertNull(userDTO.getPasswordHash());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getRole());
        assertNull(userDTO.getCreatedAt());
        assertNull(userDTO.getUpdatedAt());
    }

    @Test
    void testConstructorWithNullValues() {
        UserDTO userDTO = new UserDTO(null, null, null, null, null, null, null);

        assertNull(userDTO.getId());
        assertNull(userDTO.getUsername());
        assertNull(userDTO.getPasswordHash());
        assertNull(userDTO.getEmail());
        assertNull(userDTO.getRole());
        assertNull(userDTO.getCreatedAt());
        assertNull(userDTO.getUpdatedAt());
    }

    @Test
    void testMultipleSetterCalls() {
        UserDTO userDTO = new UserDTO(1L, "initial", "initialHash", "initial@test.com",
                "INITIAL", LocalDateTime.now(), LocalDateTime.now());

        userDTO.setId(10L);
        userDTO.setId(20L);
        userDTO.setUsername("first");
        userDTO.setUsername("second");
        userDTO.setPasswordHash("hash1");
        userDTO.setPasswordHash("hash2");
        userDTO.setEmail("email1");
        userDTO.setEmail("email2");
        userDTO.setRole("role1");
        userDTO.setRole("role2");

        LocalDateTime time1 = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime time2 = LocalDateTime.of(2024, 1, 2, 0, 0);
        userDTO.setCreatedAt(time1);
        userDTO.setCreatedAt(time2);
        userDTO.setUpdatedAt(time1);
        userDTO.setUpdatedAt(time2);

        assertEquals(20L, userDTO.getId());
        assertEquals("second", userDTO.getUsername());
        assertEquals("hash2", userDTO.getPasswordHash());
        assertEquals("email2", userDTO.getEmail());
        assertEquals("role2", userDTO.getRole());
        assertEquals(time2, userDTO.getCreatedAt());
        assertEquals(time2, userDTO.getUpdatedAt());
    }

    @Test
    void testEmptyStringValues() {
        UserDTO userDTO = new UserDTO(1L, "", "", "", "", LocalDateTime.now(), LocalDateTime.now());

        assertEquals("", userDTO.getUsername());
        assertEquals("", userDTO.getPasswordHash());
        assertEquals("", userDTO.getEmail());
        assertEquals("", userDTO.getRole());

        userDTO.setUsername("");
        userDTO.setPasswordHash("");
        userDTO.setEmail("");
        userDTO.setRole("");

        assertEquals("", userDTO.getUsername());
        assertEquals("", userDTO.getPasswordHash());
        assertEquals("", userDTO.getEmail());
        assertEquals("", userDTO.getRole());
    }

    @Test
    void testSpecialCharactersInStrings() {
        String specialUsername = "user@name#123";
        String specialPasswordHash = "hash$%^&*()";
        String specialEmail = "test+filter@example.com";
        String specialRole = "SUPER_USER-ADMIN";

        UserDTO userDTO = new UserDTO(999L, specialUsername, specialPasswordHash, specialEmail,
                specialRole, LocalDateTime.now(), LocalDateTime.now());

        assertEquals(specialUsername, userDTO.getUsername());
        assertEquals(specialPasswordHash, userDTO.getPasswordHash());
        assertEquals(specialEmail, userDTO.getEmail());
        assertEquals(specialRole, userDTO.getRole());
    }

    @Test
    void testDateTimeBoundaries() {
        LocalDateTime minDateTime = LocalDateTime.MIN;
        LocalDateTime maxDateTime = LocalDateTime.MAX;

        UserDTO userDTO = new UserDTO(1L, "user", "hash", "email@test.com", "USER",
                minDateTime, maxDateTime);

        assertEquals(minDateTime, userDTO.getCreatedAt());
        assertEquals(maxDateTime, userDTO.getUpdatedAt());

        userDTO.setCreatedAt(maxDateTime);
        userDTO.setUpdatedAt(minDateTime);

        assertEquals(maxDateTime, userDTO.getCreatedAt());
        assertEquals(minDateTime, userDTO.getUpdatedAt());
    }
}