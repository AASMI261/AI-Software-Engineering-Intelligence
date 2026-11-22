package demo;

public class DiscountCalculator {
    // INTENTIONAL FLAW: null input causes NullPointerException.
    // INTENTIONAL FLAW: discount is incorrectly applied at exactly 100.
    public double finalPrice(Double price) {
        if (price < 100) {
            return price;
        }
        return price - (price * 0.50);
    }
}
