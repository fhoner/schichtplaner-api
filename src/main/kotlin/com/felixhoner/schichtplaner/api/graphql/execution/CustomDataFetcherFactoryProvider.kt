package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.execution.SimpleKotlinDataFetcherFactoryProvider
import com.expediagroup.graphql.extensions.deepName
import com.fasterxml.jackson.databind.ObjectMapper
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import graphql.schema.DataFetcher
import graphql.schema.DataFetcherFactory
import org.springframework.beans.factory.BeanFactory
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.KProperty
import kotlin.reflect.full.findAnnotation

/**
 * Custom DataFetcherFactory to fetch data in our custom way.
 */
class CustomDataFetcherFactoryProvider(
    private val objectMapper: ObjectMapper,
    private val beanFactory: BeanFactory
) : SimpleKotlinDataFetcherFactoryProvider(objectMapper) {

    /**
     * Fetching on function-level, for example [com.felixhoner.schichtplaner.api.graphql.query.PlanQuery.getPlans].
     */
    override fun functionDataFetcherFactory(target: Any?, kFunction: KFunction<*>) = DataFetcherFactory {
        val authorised = kFunction.findAnnotation<Authorized>()
        val reactiveDataFetcher = ReactiveFunctionDataFetcher(
            target = target,
            fn = kFunction,
            objectMapper = objectMapper
        )
        when {
            authorised != null -> AuthorizedDataFetcher(reactiveDataFetcher, authorised.roles.toList())
            else -> reactiveDataFetcher
        }
    }

    /**
     * Fetching on property-level. There are two types:
     * 1. non-lateinit properties where queried values can directly be extracted from the field.
     *  Example: [com.felixhoner.schichtplaner.api.graphql.dto.PlanDto.name]
     * 2. lateinit properties, where another function will be called to retrieve the requested
     * value. This function will be found by reflection, so the property name and the bean name
     * must match on the implemented criteria below.
     * Example: [com.felixhoner.schichtplaner.api.graphql.dto.PlanDto.productions]
     */
    @Suppress("UNCHECKED_CAST")
    override fun propertyDataFetcherFactory(kClass: KClass<*>, kProperty: KProperty<*>): DataFetcherFactory<Any?> = DataFetcherFactory {
        val authorized = kProperty.findAnnotation<Authorized>()
        if (kProperty.isLateinit) {
            val targetedTypeName = it?.fieldDefinition?.type?.deepName?.removeSuffix("!")?.removeSuffix("Input")
            val dataFetcherBean = beanFactory.getBean("${targetedTypeName}DataFetcher") as DataFetcher<Any?>
            when {
                authorized != null -> AuthorizedDataFetcher(dataFetcherBean, authorized.roles.toList())
                else -> dataFetcherBean
            }
        } else {
            val reactiveDataFetcher = ReactivePropertyDataFetcher(kProperty)
            when {
                authorized != null -> AuthorizedDataFetcher(reactiveDataFetcher, authorized.roles.toList())
                else -> reactiveDataFetcher
            }
        }
    }
}
