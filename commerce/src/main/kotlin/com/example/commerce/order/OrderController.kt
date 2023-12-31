package com.example.commerce.order

import com.example.commerce.order.OrderService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    fun createOrder(@RequestBody orderRequest : Order) {
        // 요청값 검증

        orderService.sendOrder(orderRequest)
        // 응답값 반환
    }
}