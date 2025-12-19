package exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class InconsistentFunctionsExceptionTest {

    @Test
    void testDefaultConstructor() {
        InconsistentFunctionsException exception = new InconsistentFunctionsException();
        assertNull(exception.getMessage());
    }

    @Test
    void testConstructorWithMessage() {
        String message = "Test message";
        InconsistentFunctionsException exception = new InconsistentFunctionsException(message);
        assertEquals(message, exception.getMessage());
    }
}