package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContext
import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContextFactory
import kotlin.coroutines.coroutineContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono

class GraphQLSecurityContext(
    request: ServerRequest,
    val securityContext: Mono<SecurityContext>
) : SpringGraphQLContext(request)

class ReactiveSecurityContextFactory : SpringGraphQLContextFactory<GraphQLSecurityContext>() {

    @ExperimentalCoroutinesApi
    override suspend fun generateContext(request: ServerRequest): GraphQLSecurityContext {
        val reactorContext = coroutineContext[ReactorContext]?.context ?: throw RuntimeException("reactor context unavailable")
        val securityContext = reactorContext.getOrDefault<Mono<SecurityContext>>(SecurityContext::class.java, Mono.empty())!!
        return GraphQLSecurityContext(
            request,
            securityContext = securityContext
        )
    }
}
