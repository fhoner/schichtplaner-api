package com.felixhoner.schichtplaner.api.security

import com.felixhoner.schichtplaner.api.business.exception.InvalidTokenException
import mu.KotlinLogging
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Configuration
class GlobalErrorHandler : ErrorWebExceptionHandler {

    private val logger = KotlinLogging.logger {}

    override fun handle(serverWebExchange: ServerWebExchange, throwable: Throwable): Mono<Void> {
        logger.error("Exception thrown: ", throwable)
        val bufferFactory = serverWebExchange.response.bufferFactory()

        serverWebExchange.response.statusCode = when (throwable) {
            is InvalidTokenException -> UNAUTHORIZED
            is ResponseStatusException -> throwable.status
            else -> INTERNAL_SERVER_ERROR
        }

        val dataBuffer: DataBuffer = bufferFactory.wrap("".toByteArray())
        return serverWebExchange.response.writeWith(Mono.just(dataBuffer))
    }
}
