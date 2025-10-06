package exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InterpolationExceptionTest {

    @Test
    void constructorWithoutMessage_shouldCreateExceptionWithNullMessage() {
        InterpolationException exception = new InterpolationException();
        assertNull(exception.getMessage());
    }

    @Test
    void constructorWithMessage_shouldStoreTheProvidedMessage() {
        String expectedMessage = "Interpolation is not possible for this input";
        InterpolationException exception = new InterpolationException(expectedMessage);
        assertEquals(expectedMessage, exception.getMessage());
    }

}