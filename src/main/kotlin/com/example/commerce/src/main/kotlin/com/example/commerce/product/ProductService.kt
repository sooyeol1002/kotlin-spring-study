package com.example.commerce.product

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.*

@Service
class ProductService(private val productClient: ProductClient) {

    @Scheduled(fixedRate = 1000 * 5)
    fun scheduledFetchTopPromotion() {
        println("--called by schedule: ${Date().time}--")
        val result = productClient.getTopPromotion()
        println(result)
    }
}