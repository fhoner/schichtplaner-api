package com.felixhoner.schichtplaner.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.felixhoner.schichtplaner.api.graphql.errorhandling.CustomDataFetcherExceptionHandler
import com.felixhoner.schichtplaner.api.graphql.execution.CustomDataFetcherFactoryProvider
import graphql.execution.DataFetcherExceptionHandler
import org.springframework.beans.factory.BeanFactory
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
		objectMapper: ObjectMapper,
		applicationContext: ApplicationContext,
		beanFactory: BeanFactory
	) = CustomDataFetcherFactoryProvider(objectMapper, beanFactory)

	@Bean
	fun dataFetcherExceptionHandler(): DataFetcherExceptionHandler = CustomDataFetcherExceptionHandler()

}

fun main(args: Array<String>) {
	runApplication<SchichtplanerBackend>(*args)
}
