package com.felixhoner.schichtplaner.api.graphql.config

import com.expediagroup.graphql.execution.SimpleKotlinDataFetcherFactoryProvider
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.schema.DataFetcherFactory
import org.springframework.context.ApplicationContext
import kotlin.reflect.*

/**
 * Custom DataFetcherFactory provider that returns custom Spring based DataFetcherFactory for resolving lateinit properties.
 */
class CustomDataFetcherFactoryProvider(
	private val springDataFetcherFactory: SpringDataFetcherFactory,
	private val objectMapper: ObjectMapper,
	private val applicationContext: ApplicationContext
): SimpleKotlinDataFetcherFactoryProvider(objectMapper) {

	override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) = DataFetcherFactory {
		CustomFunctionDataFetcher(
			target = target,
			fn = kFunction,
			objectMapper = objectMapper,
			appContext = applicationContext
		)
	}

	override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>): DataFetcherFactory<Any?> =
		if (kProperty.isLateinit) {
			springDataFetcherFactory
		} else {
			super.propertyDataFetcherFactory(kClass, kProperty)
		}
}
