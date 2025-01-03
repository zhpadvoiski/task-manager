package com.example

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.assertContains
import org.junit.Test
import org.junit.Assert.assertEquals

fun testScope (block: suspend ApplicationTestBuilder.() -> Unit): Unit = testApplication {
    application {
        module()
    }

    block()
}

class ApplicationTest {
    @Test
    fun testRoot() = testScope {
        val response = client.get("/")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello World!", response.bodyAsText())
    }

    @Test
    fun `should handle test1 route`() = testScope {
        val response = client.get("/test1")

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("html", response.contentType()?.contentSubtype)
        assertContains("Hello from Ktor!", response.bodyAsText())
    }

    @Test
    fun `task should be found by priority`() = testScope {
        val response = client.get("/tasks/byPriority/Medium")


        assertEquals(HttpStatusCode.OK, response.status)
        assertContains(response.bodyAsText(), "Mow the lawn")
        assertContains(response.bodyAsText(), "Paint the fence")
    }

    @Test
    fun `should produce 400 for invalid property`() = testScope {
        val response = client.get("/tasks/byPriority/Invalid")

        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `should return 404 for unused property`() = testScope {
        val response = client.get("/tasks/byPriority/Vital")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }
}
