package com.felixhoner.schichtplaner.api.graphql.execution

import graphql.schema.DataFetchingEnvironment
import graphql.schema.PropertyDataFetcher
import kotlin.reflect.KProperty
import reactor.core.publisher.Mono

class ReactivePropertyDataFetcher(kProperty: KProperty<*>) : PropertyDataFetcher<Any?>(kProperty.name) {
    override fun get(environment: DataFetchingEnvironment): Any? = when (val result = super.get(environment)) {
        is Mono<*> -> result.toFuture()
        else -> result
    }
}
