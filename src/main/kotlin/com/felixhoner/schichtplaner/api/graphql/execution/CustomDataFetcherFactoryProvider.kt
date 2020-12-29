package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.execution.SimpleKotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import graphql.schema.DataFetcherFactory
import kotlin.reflect.*
import kotlin.reflect.full.findAnnotation

class CustomDataFetcherFactoryProvider(
	private val defaultDataFetcherFactory: DefaultDataFetcherFactory,
	private val objectMapper: ObjectMapper
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
			super.propertyDataFetcherFactory(kClass, kProperty)
		}
}
