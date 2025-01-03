package com.example

import com.example.model.Priority
import com.example.model.TaskRepository
import com.example.model.tasksAsTable
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}")
        }
    }

    routing {
        staticResources("/content", "mycontent", "example.html")

        get("/") {
            call.respondText("Hello World!")
        }

        get("/test1") {
            call.respondText(
                contentType = ContentType.parse("text/html"),
                text = "Hello from Ktor!"
            )
        }

        get("/error-state") {
            throw IllegalStateException("Too Busy")
        }

        get("/tasks") {
            call.respondText(
                contentType = ContentType.parse("text/html"),
                text = TaskRepository.allTasks().tasksAsTable())
        }

        get("/tasks/byPriority/{priority}") {
            val priorityAsText = call.parameters["priority"]

            if(priorityAsText.isNullOrEmpty()){
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            try {
                val priority = Priority.valueOf(priorityAsText)
                val tasks = TaskRepository.taskByPriority(priority)

                if(tasks.isEmpty()) {
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = tasks.tasksAsTable()
                )
            } catch(e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest)
            }
        }
    }
}
