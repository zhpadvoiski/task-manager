package com.example

import com.example.model.Priority
import com.example.model.Task
import com.example.model.TaskRepository
import com.example.model.tasksAsTable
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.JsonConvertException
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    install(StatusPages) {
        exception<IllegalStateException> { call, cause ->
            call.respondText("App in illegal state as ${cause.message}")
        }
    }
    install(ContentNegotiation) {
        json()
    }

    routing {
        staticResources("/content", "mycontent", "example.html")
        staticResources("/task-ui", "task-ui")
        staticResources("/static-form", "static-form")

        staticResources("/site", "static", "index.html")
        staticResources("/static", "static/static")

        route("/tasks"){
            get {
                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = TaskRepository.allTasks().tasksAsTable())
            }

            get("/byPriority/{priority}") {
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

            get("/byName/{taskName}") {
                val name = call.parameters["taskName"]
                if(name.isNullOrEmpty()){
                    call.respond(HttpStatusCode.BadRequest)
                    return@get
                }

                val task = TaskRepository.taskByName(name)
                if(task == null){
                    call.respond(HttpStatusCode.NotFound)
                    return@get
                }

                call.respondText(
                    contentType = ContentType.parse("text/html"),
                    text = listOf(task).tasksAsTable()
                )
            }

            post {
                val allowedParams = listOf("name", "description", "priority")
                val formContent = call.receiveParameters()

                val params = allowedParams.map { formContent[it] ?: "" }.let { (name, description, priority) ->
                    Triple(name, description, priority)
                }

                if(params.toList().any { it.isEmpty() }){
                    call.respond(HttpStatusCode.BadRequest)
                    return@post
                }

                try {
                    val ( name, description, priorityVal ) = params
                    val priority = Priority.valueOf(priorityVal)

                    TaskRepository.addTask(Task(name, description, priority))

                    call.respond(HttpStatusCode.NoContent)
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
                catch (ex: IllegalStateException) {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }

        route("/api") {
            route("/tasks"){
                get {
                    call.respond(TaskRepository.allTasks())
                }

                get("/byPriority/{priority}") {
                    val priorityParam = call.parameters["priority"]

                    if(priorityParam.isNullOrEmpty()){
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    try {
                        val priority = Priority.valueOf(priorityParam)
                        val tasks = TaskRepository.taskByPriority(priority)

                        if(tasks.isEmpty()){
                            call.respond(HttpStatusCode.NotFound)
                            return@get
                        }

                        call.respond(tasks)

                    } catch(e: IllegalArgumentException) {
                        call.respond(HttpStatusCode.BadRequest)
                    }

                }

                get("/byName/{name}") {
                    val name = call.parameters["name"]

                    if(name.isNullOrEmpty()){
                        call.respond(HttpStatusCode.BadRequest)
                        return@get
                    }

                    val task = TaskRepository.taskByName(name)
                    if(task == null){
                        call.respond(HttpStatusCode.NotFound)
                        return@get
                    }

                    call.respond(task)
                }

                post {
                    try {
                        val task = call.receive<Task>()
                        TaskRepository.addTask(task)
                        call.respond(HttpStatusCode.Created)
                    } catch (e: java.lang.IllegalStateException) {
                        call.respond(HttpStatusCode.BadRequest)
                    } catch (e: JsonConvertException) {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                delete("/{taskName}") {
                    val taskName = call.parameters["taskName"]
                    if (taskName.isNullOrEmpty()){
                        call.respond(HttpStatusCode.BadRequest)
                        return@delete
                    }

                    if (TaskRepository.deleteTask(taskName)){
                        call.respond(HttpStatusCode.NoContent)
                    } else {
                        call.respond(HttpStatusCode.NotFound)
                    }
                }
            }

        }

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
    }
}
