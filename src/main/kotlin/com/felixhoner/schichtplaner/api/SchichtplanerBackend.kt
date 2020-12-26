package com.felixhoner.schichtplaner.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.felixhoner.schichtplaner.api.graphql.config.CustomDataFetcherFactoryProvider
import com.felixhoner.schichtplaner.api.graphql.config.SpringDataFetcherFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SchichtplanerBackend {

	@Bean
	fun dataFetcherFactoryProvider(
		springDataFetcherFactory: SpringDataFetcherFactory,
		objectMapper: ObjectMapper,
		applicationContext: ApplicationContext
	) = CustomDataFetcherFactoryProvider(springDataFetcherFactory, objectMapper, applicationContext)

}

fun main(args: Array<String>) {
	runApplication<SchichtplanerBackend>(*args)
}
