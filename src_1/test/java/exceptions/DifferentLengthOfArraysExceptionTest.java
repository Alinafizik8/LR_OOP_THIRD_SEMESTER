package exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DifferentLengthOfArraysExceptionTest {

    @Test
    void constructorWithoutMessage_shouldCreateExceptionWithNullMessage() {
        DifferentLengthOfArraysException exception = new DifferentLengthOfArraysException();
        assertNull(exception.getMessage());
    }

    @Test
    void constructorWithMessage_shouldCreateExceptionWithCorrectMessage() {
        String expectedMessage = "Arrays have different lengths";
        DifferentLengthOfArraysException exception = new DifferentLengthOfArraysException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }

}