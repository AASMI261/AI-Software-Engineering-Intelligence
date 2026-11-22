package demo;

public class Calculator {
    // Intentional edge case: division by zero is not handled.
    public int divide(int numerator, int denominator) {
        return numerator / denominator;
    }

    // Intentional logic bug: adds 2 rather than 1.
    public int addOne(int value) {
        return value + 2;
    }
}
