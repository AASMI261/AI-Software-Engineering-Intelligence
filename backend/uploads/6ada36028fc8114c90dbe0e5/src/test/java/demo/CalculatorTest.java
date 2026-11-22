package demo;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CalculatorTest {
    private final Calculator calculator = new Calculator();

    @Test
    void divideNormalNumbers() {
        assertEquals(2, calculator.divide(10, 5));
    }

    @Test
    void addOneShouldAddOne() {
        // Expected to fail: addOne(5) currently returns 7, not 6.
        assertEquals(6, calculator.addOne(5));
    }
}
