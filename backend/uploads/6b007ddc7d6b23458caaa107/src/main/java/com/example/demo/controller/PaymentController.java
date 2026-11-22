package com.example.demo.controller;

import com.example.demo.service.OrderService;

public class PaymentController {

    private final OrderService orderService = new OrderService();

    // TODO: Add proper input validation and structured logging
    public String processPayment(String orderId) {
        System.out.println("Processing payment for order: " + orderId);
        if (orderId == null || orderId.isBlank()) {
            System.out.println("Invalid order ID provided");
            return "INVALID_ORDER";
        }
        return orderService.findOrderById(orderId);
    }
}
