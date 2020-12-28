package com.felixhoner.schichtplaner.api.graphql.config

import com.expediagroup.graphql.annotations.GraphQLDirective
import com.expediagroup.graphql.execution.*
import com.expediagroup.graphql.spring.exception.SimpleKotlinGraphQLError
import com.expediagroup.graphql.spring.execution.GraphQLContextFactory
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.execution.DataFetcherResult
import graphql.schema.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.reactor.ReactorContext
import org.springframework.context.ApplicationContext
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasAnyAuthority
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.core.context.SecurityContext
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture
import kotlin.coroutines.coroutineContext
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation

/**
 * Custom DataFetcherFactory provider that returns custom Spring based DataFetcherFactory for resolving lateinit properties.
 */
class CustomDataFetcherFactoryProvider(
	private val springDataFetcherFactory: SpringDataFetcherFactory,
	private val objectMapper: ObjectMapper,
	private val applicationContext: ApplicationContext
): SimpleKotlinDataFetcherFactoryProvider(objectMapper) {

	override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) = DataFetcherFactory {
		val authorised = kFunction.findAnnotation<Authorised>()
		val reactiveDataFetcher = ReactiveFunctionDataFetcher(
			target = target,
			fn = kFunction,
			objectMapper = objectMapper
		)
		when {
			authorised != null -> AuthorisedDataFetcher(reactiveDataFetcher, authorised.roles.toList())
			else               -> reactiveDataFetcher
		}
	}

	override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>): DataFetcherFactory<Any?> =
		if (kProperty.isLateinit) {
			springDataFetcherFactory
		} else {
			super.propertyDataFetcherFactory(kClass, kProperty)
		}
}

class AuthorisedDataFetcher(
	private val originalDataFetcher: DataFetcher<Any?>,
	private val requiredRoles: Collection<String>
): DataFetcher<Any?> {

	override fun get(environment: DataFetchingEnvironment): Any {
		if (requiredRoles.isEmpty()) {
			return originalDataFetcher.get(environment)!!
		}

		val context: GraphQLSecurityContext? = environment.getContext<GraphQLSecurityContext>()
		val securityContext: Mono<SecurityContext> =
			context?.securityContext ?: throw RuntimeException("SecurityContext not present")
		val accessCheck = checkRoles(securityContext)

		return accessCheck
			.filter { it.isGranted }
			.map { createResult(originalDataFetcher.get(environment)!!) }
			.switchIfEmpty(Mono.just(createGraphQLError(environment)))
			.toFuture() // FIXME: how to avoid .block() also not allowed
	}

	private fun checkRoles(securityContext: Mono<SecurityContext>): Mono<AuthorizationDecision> {
		val voter = hasAnyAuthority<Any>(*requiredRoles.toTypedArray())
		return voter.check(securityContext.map { it.authentication }, null).defaultIfEmpty(AuthorizationDecision(false))
	}

	private fun createGraphQLError(environment: DataFetchingEnvironment): DataFetcherResult<Any> {
		val error = SimpleKotlinGraphQLError(
			RuntimeException("Role(s) ${requiredRoles.joinToString(separator = ",")} required"),
			listOf(environment.field.sourceLocation),
			environment.executionStepInfo.path.toList()
		)
		return DataFetcherResult.newResult<Any>()
			.error(error)
			.build()
	}

	private fun createResult(data: Any): DataFetcherResult<*> {
		return DataFetcherResult.newResult<Any>()
			.data((data as? CompletableFuture<*>)?.get() ?: data) // FIXME: how to avoid
			.build()
	}
}

class GraphQLSecurityContext(val securityContext: Mono<SecurityContext>): GraphQLContext

@Component
class ReactiveSecurityContextFactory: GraphQLContextFactory<GraphQLSecurityContext> {

	@ExperimentalCoroutinesApi
	override suspend fun generateContext(request: ServerHttpRequest, response: ServerHttpResponse): GraphQLSecurityContext {
		val reactorContext = coroutineContext[ReactorContext]?.context ?: throw RuntimeException("reactor context unavailable")
		val securityContext = reactorContext.getOrDefault<Mono<SecurityContext>>(SecurityContext::class.java, Mono.empty())!!
		return GraphQLSecurityContext(securityContext = securityContext)
	}
}

@GraphQLDirective(name = "authorised", description = "Used to check authorisation")
annotation class Authorised(vararg val roles: String)

class ReactiveFunctionDataFetcher(target: Any?, fn: KFunction<*>, objectMapper: ObjectMapper):
	FunctionDataFetcher(target, fn, objectMapper) {

	override fun get(environment: DataFetchingEnvironment): Any? = when (val result = super.get(environment)) {
		is Mono<*> -> result.toFuture()
		else       -> result
	}
}
