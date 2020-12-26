package com.felixhoner.schichtplaner.api.graphql.fetching

import com.expediagroup.graphql.spring.operations.Query
import com.felixhoner.schichtplaner.api.graphql.dto.*
import com.felixhoner.schichtplaner.api.service.PlanService
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
@Scope("prototype")
@Suppress("unused")
class PlanQuery(
	private val planService: PlanService,
	private val transformer: TransformerDto
): Query {

	fun getPlans(): List<PlanDto> = planService.getAll().map(transformer::toDto)

}

@Component("[ProductionDto!]DataFetcher")
@Scope("prototype")
class ProductionsDataFetcher: DataFetcher<CompletableFuture<List<ProductionDto>>>, BeanFactoryAware {

	private lateinit var beanFactory: BeanFactory

	override fun setBeanFactory(beanFactory: BeanFactory) {
		this.beanFactory = beanFactory
	}

	override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<ProductionDto>> {
		val planId = environment.getSource<PlanDto>().id
		return environment
			.getDataLoader<Long, List<ProductionDto>>("productionLoader")
			.load(planId)
	}
}
