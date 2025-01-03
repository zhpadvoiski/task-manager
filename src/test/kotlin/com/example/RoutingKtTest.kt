package com.example

import io.ktor.client.request.*
import io.ktor.server.testing.*
import kotlin.test.Test

class RoutingKtTest {

    @Test
    fun testGetTask() = testApplication {
        application {
            TODO("Add the Ktor module for the test")
        }
        client.get("/task").apply {
            TODO("Please write your test here")
        }
    }
}
