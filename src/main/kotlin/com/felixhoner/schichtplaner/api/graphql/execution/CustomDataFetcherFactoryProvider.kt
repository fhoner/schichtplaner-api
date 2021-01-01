package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.execution.SimpleKotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import graphql.schema.*
import reactor.core.publisher.Mono
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation

class CustomDataFetcherFactoryProvider(
	private val defaultDataFetcherFactory: DefaultDataFetcherFactory,
	private val objectMapper: ObjectMapper,
): SimpleKotlinDataFetcherFactoryProvider(objectMapper) {

	override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) = DataFetcherFactory {
		val authorised = kFunction.findAnnotation<Authorized>()
		val reactiveDataFetcher = ReactiveFunctionDataFetcher(
			target = target,
			fn = kFunction,
			objectMapper = objectMapper
		)
		when {
			authorised != null -> AuthorizedDataFetcher(reactiveDataFetcher, authorised.roles.toList())
			else               -> reactiveDataFetcher
		}
	}

	override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>): DataFetcherFactory<Any?> =
		if (kProperty.isLateinit) {
			defaultDataFetcherFactory
		} else {
			DataFetcherFactory {
				val authorized = kProperty.findAnnotation<Authorized>()
				val reactiveDataFetcher = ReactivePropertyDataFetcher(kProperty)
				when {
					authorized != null -> AuthorizedDataFetcher(reactiveDataFetcher, authorized.roles.toList())
					else               -> reactiveDataFetcher
				}
			}
		}
}

class ReactivePropertyDataFetcher(kProperty: KProperty<*>):
	PropertyDataFetcher<Any?>(kProperty.name) {

	override fun get(environment: DataFetchingEnvironment): Any? = when (val result = super.get(environment)) {
		is Mono<*> -> result.toFuture()
		else       -> result
	}
}
