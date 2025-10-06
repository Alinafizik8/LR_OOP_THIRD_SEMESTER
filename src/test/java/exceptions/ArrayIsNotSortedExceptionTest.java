package exceptions;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ArrayIsNotSortedExceptionTest {

    @Test
    void constructorWithoutMessage_shouldCreateExceptionWithNullMessage() {
        ArrayIsNotSortedException exception = new ArrayIsNotSortedException();
        assertNull(exception.getMessage());
    }

    @Test
    void constructorWithMessage_shouldCreateExceptionWithCorrectMessage() {
        String message = "Array must be sorted in ascending order";
        ArrayIsNotSortedException exception = new ArrayIsNotSortedException(message);
        assertEquals(message, exception.getMessage());
    }

}