package com.felixhoner.schichtplaner.api.graphql

import com.expediagroup.graphql.server.spring.execution.SpringGraphQLContextFactory
import com.fasterxml.jackson.databind.ObjectMapper
import com.felixhoner.schichtplaner.api.graphql.errorhandling.CustomDataFetcherExceptionHandler
import com.felixhoner.schichtplaner.api.graphql.execution.CustomDataFetcherFactoryProvider
import com.felixhoner.schichtplaner.api.graphql.execution.ReactiveSecurityContextFactory
import graphql.execution.DataFetcherExceptionHandler
import org.springframework.beans.factory.BeanFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class GraphQLConfig {

    @Bean
    fun dataFetcherFactoryProvider(objectMapper: ObjectMapper, beanFactory: BeanFactory) =
        CustomDataFetcherFactoryProvider(objectMapper, beanFactory)

    @Bean
    fun springGraphQLContextFactory(): SpringGraphQLContextFactory<*> = ReactiveSecurityContextFactory()

    @Bean
    fun dataFetcherExceptionHandler(): DataFetcherExceptionHandler = CustomDataFetcherExceptionHandler()

}
