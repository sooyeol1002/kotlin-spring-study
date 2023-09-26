package com.example.commerce.inventory

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "inventoryClient", url = "http://192.168.100.152:8082//inventories")
interface InventoryClient {

    @GetMapping("/{productId}")
    fun fetchProductStocks(@PathVariable productId : Int) : Int?
}