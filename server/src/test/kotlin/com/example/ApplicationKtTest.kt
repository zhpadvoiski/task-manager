package com.example

import com.example.model.Priority
import com.example.model.Task
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import kotlin.test.assertContains
import kotlin.test.assertContentEquals
import org.junit.Test
import org.junit.Assert.assertEquals

fun testScope (block: suspend ApplicationTestBuilder.() -> Unit): Unit = testApplication {
    application {
        module()
    }

    block()
}

class ApplicationKtTest {
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

    @Test
    fun `should add new task`() = testScope {
        val response = client.post("/tasks") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "name" to "swimming",
                    "description" to "Go to the beach",
                    "priority" to "Low"
                ).formUrlEncode()
            )
        }

        assertEquals(HttpStatusCode.NoContent, response.status)

        val response2 = client.get("/tasks")

        assertEquals(HttpStatusCode.OK, response2.status)

        val body = response2.bodyAsText()
        assertContains(body, "swimming")
        assertContains(body, "Go to the beach")
    }

    @Test
    fun `should return 400 when not all parameters supplied`() = testScope {
        val request = client.post("/tasks") {
            header(HttpHeaders.ContentType, ContentType.Application.FormUrlEncoded.toString())
            setBody(
                listOf(
                    "name" to "swimming",
                    "description" to "Go to the beach"
                ).formUrlEncode()
            )
        }

        assertEquals(HttpStatusCode.BadRequest, request.status)
    }

    @Test
    fun `should find task by name`() = testScope {
        val response = client.get("/tasks/byName/cleaning")

        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText();
        assertContains(body, "Clean the house")
    }

    @Test
    fun `should return 404 when no task with name`() = testScope {
        val response = client.get("/tasks/byName/fail")

        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `should return json for api`() = testScope {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.get("/api/tasks")
        val tasks = response.body<List<Task>>()

        val tasksNames = tasks.map(Task::name)
        val expectedTaskNames = listOf("cleaning", "gardening", "shopping", "painting")

        assertContentEquals(tasksNames, expectedTaskNames)
    }

    @Test
    fun `should post task through api`() = testScope {
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        val task = Task("reading", "Read the Harry Potter book", Priority.Medium)
        val response = client.post("/api/tasks"){
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(task)
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val response2 = client.get("/api/tasks/byName/reading")
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals(task.name, response2.body<Task>().name)
    }
}
