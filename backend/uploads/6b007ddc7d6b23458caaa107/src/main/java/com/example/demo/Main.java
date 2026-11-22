package com.example.demo;

import com.example.demo.controller.PaymentController;

public class Main {
    public static void main(String[] args) {
        PaymentController controller = new PaymentController();
        java.util.logging.Logger.getLogger(Main.class.getName()).info("Order status: " + controller.processPayment("ORD-101"));
    }
}
