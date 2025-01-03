package com.example

import com.example.model.TaskRepository
import com.example.model.tasksAsTable
import io.ktor.http.ContentType
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        staticResources("/content", "mycontent", "example.html")
        get("/") {
            call.respondText("Hello World!")
        }

        get("/tasks") {
            call.respondText(
                contentType = ContentType.parse("text/html"),
                text = TaskRepository.allTasks().tasksAsTable())
        }
    }
}
