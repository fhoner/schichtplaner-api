package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.spring.exception.SimpleKotlinGraphQLError
import graphql.execution.DataFetcherResult
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.security.authorization.AuthorityReactiveAuthorizationManager
import org.springframework.security.authorization.AuthorizationDecision
import org.springframework.security.core.context.SecurityContext
import reactor.core.publisher.Mono
import java.util.concurrent.CompletableFuture

class AuthorizedDataFetcher(
    private val originalDataFetcher: DataFetcher<Any?>,
    private val requiredRoles: Collection<String>
) : DataFetcher<Any?> {

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
        val requiredRolesPrefixed = requiredRoles.map { "ROLE_$it" }
        val voter = AuthorityReactiveAuthorizationManager.hasAnyAuthority<Any>(*requiredRolesPrefixed.toTypedArray())
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
