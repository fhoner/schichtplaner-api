package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.generator.execution.FunctionDataFetcher
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetchingEnvironment
import kotlin.reflect.KFunction
import reactor.core.publisher.Mono

class ReactiveFunctionDataFetcher(target: Any?, fn: KFunction<*>, objectMapper: ObjectMapper) :
    FunctionDataFetcher(target, fn, objectMapper) {

    override fun get(environment: DataFetchingEnvironment): Any? = when (val result = super.get(environment)) {
        is Mono<*> -> result.toFuture()
        else -> result
    }
}
