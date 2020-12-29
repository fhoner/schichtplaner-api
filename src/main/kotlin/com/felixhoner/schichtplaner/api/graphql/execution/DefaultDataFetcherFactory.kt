package com.felixhoner.schichtplaner.api.graphql.execution

import com.expediagroup.graphql.extensions.deepName
import graphql.schema.*
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.stereotype.Component

@Component
class DefaultDataFetcherFactory: DataFetcherFactory<Any?>, BeanFactoryAware {
	private lateinit var beanFactory: BeanFactory

	override fun setBeanFactory(beanFactory: BeanFactory) {
		this.beanFactory = beanFactory
	}

	@Suppress("UNCHECKED_CAST")
	override fun get(environment: DataFetcherFactoryEnvironment?): DataFetcher<Any?> {
		// Strip out possible `Input` and `!` suffixes added to by the SchemaGenerator
		val targetedTypeName = environment?.fieldDefinition?.type?.deepName?.removeSuffix("!")?.removeSuffix("Input")
		return beanFactory.getBean("${targetedTypeName}DataFetcher") as DataFetcher<Any?>
	}
}
