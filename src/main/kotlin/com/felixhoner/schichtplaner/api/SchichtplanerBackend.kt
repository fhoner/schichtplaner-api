package com.felixhoner.schichtplaner.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.felixhoner.schichtplaner.api.graphql.execution.CustomDataFetcherFactoryProvider
import com.felixhoner.schichtplaner.api.graphql.execution.DefaultDataFetcherFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity

@SpringBootApplication
@EnableReactiveMethodSecurity
class SchichtplanerBackend {

	@Bean
	fun dataFetcherFactoryProvider(
		defaultDataFetcherFactory: DefaultDataFetcherFactory,
		objectMapper: ObjectMapper,
		applicationContext: ApplicationContext
	) = CustomDataFetcherFactoryProvider(defaultDataFetcherFactory, objectMapper)

}

fun main(args: Array<String>) {
	runApplication<SchichtplanerBackend>(*args)
}
