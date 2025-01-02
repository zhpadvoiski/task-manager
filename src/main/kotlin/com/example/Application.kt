package com.example

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val port = environment.config.propertyOrNull("ktor.deployment.port")?.getString()
    println("Port: $port")
    configureRouting()
}
