package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.execution.GraphQLContext
import com.expediagroup.graphql.spring.execution.GraphQLContextFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import kotlin.coroutines.coroutineContext

class GraphQLSecurityContext(
	val securityContext: Mono<SecurityContext>,
	val response: ServerHttpResponse
): GraphQLContext

@Component
class ReactiveSecurityContextFactory: GraphQLContextFactory<GraphQLSecurityContext> {

	@ExperimentalCoroutinesApi
	override suspend fun generateContext(request: ServerHttpRequest, response: ServerHttpResponse): GraphQLSecurityContext {
		val reactorContext = coroutineContext[ReactorContext]?.context ?: throw RuntimeException("reactor context unavailable")
		val securityContext = reactorContext.getOrDefault<Mono<SecurityContext>>(SecurityContext::class.java, Mono.empty())!!
		return GraphQLSecurityContext(
			securityContext = securityContext,
			response = response
		)
	}
}
