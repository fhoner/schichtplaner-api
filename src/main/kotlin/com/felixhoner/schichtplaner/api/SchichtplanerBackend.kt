package com.felixhoner.schichtplaner.api

import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContextFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.felixhoner.schichtplaner.api.graphql.errorhandling.CustomDataFetcherExceptionHandler
import com.felixhoner.schichtplaner.api.graphql.execution.CustomDataFetcherFactoryProvider
import com.felixhoner.schichtplaner.api.graphql.execution.ReactiveSecurityContextFactory
import graphql.execution.DataFetcherExceptionHandler
import org.springframework.beans.factory.BeanFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity

@SpringBootApplication
@EnableReactiveMethodSecurity
class SchichtplanerBackend {

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().registerModule(KotlinModule())

    @Bean
    fun dataFetcherFactoryProvider(objectMapper: ObjectMapper, beanFactory: BeanFactory) =
        CustomDataFetcherFactoryProvider(objectMapper, beanFactory)

    @Bean
    fun springGraphQLContextFactory(): SpringGraphQLContextFactory<*> = ReactiveSecurityContextFactory()

    @Bean
    fun dataFetcherExceptionHandler(): DataFetcherExceptionHandler = CustomDataFetcherExceptionHandler()

}

fun main(args: Array<String>) {
    runApplication<SchichtplanerBackend>(*args)
}
