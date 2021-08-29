package com.felixhoner.schichtplaner.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity

@SpringBootApplication
@EnableReactiveMethodSecurity
class SchichtplanerBackend

fun main(args: Array<String>) {
    runApplication<SchichtplanerBackend>(*args)
}
