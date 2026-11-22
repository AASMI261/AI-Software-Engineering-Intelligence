package com.example.demo;

import com.example.demo.controller.PaymentController;

public class Main {
    public static void main(String[] args) {
        PaymentController controller = new PaymentController();
        System.out.println("Order status: " + controller.processPayment("ORD-101"));
    }
}
