package com.felixhoner.schichtplaner.api.graphql.query

import com.expediagroup.graphql.spring.exception.SimpleKotlinGraphQLError
import com.expediagroup.graphql.spring.operations.Query
import com.felixhoner.schichtplaner.api.business.service.PlanService
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import com.felixhoner.schichtplaner.api.graphql.dto.*
import com.felixhoner.schichtplaner.api.graphql.execution.GraphQLSecurityContext
import graphql.execution.DataFetcherResult
import graphql.language.Field
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture
import kotlin.reflect.full.declaredMemberProperties

@Component
@Scope("prototype")
@Suppress("unused")
class PlanQuery(
	private val planService: PlanService,
	private val transformer: TransformerDto
): Query {

	@Authorized("ROLE_USER")
	fun getPlans() = planService.getAll().map(transformer::toDto)

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

@Component("[ShiftDto!]DataFetcher")
@Scope("prototype")
class ShiftsDataFetcher: DataFetcher<CompletableFuture<List<ShiftDto>>>, BeanFactoryAware {

	private lateinit var beanFactory: BeanFactory

	override fun setBeanFactory(beanFactory: BeanFactory) {
		this.beanFactory = beanFactory
	}

	@Authorized("MANAGER")
	override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<ShiftDto>> {
		val productionId = environment.getSource<ProductionDto>().id
		return environment
			.getDataLoader<Long, List<ShiftDto>>("shiftLoader")
			.load(productionId)
	}
}

@Component("[WorkerDto!]DataFetcher")
@Scope("prototype")
class WorkersDataFetcher: DataFetcher<CompletableFuture<List<WorkerDto>>>, BeanFactoryAware {

	private lateinit var beanFactory: BeanFactory

	override fun setBeanFactory(beanFactory: BeanFactory) {
		this.beanFactory = beanFactory
	}

	override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<WorkerDto>> {
		val productionId = environment.getSource<ShiftDto>().id
		val context = environment.getContext<GraphQLSecurityContext>()

		val fields: List<String> = environment.mergedField.fields[0].selectionSet.selections.map { (it as Field).name }
		val annotated: List<Annotation> = WorkerDto::class.declaredMemberProperties
			.filter { fields.contains(it.name) }
			.flatMap { it.annotations }
		val requiredRoles: List<String> = annotated.filterIsInstance<Authorized>().flatMap { it.roles.toList() }

		return context.securityContext
			.flatMap {
				val userRoles: List<String> = it.authentication.authorities.map { au -> au.authority }
				val missingRoles: List<String> = requiredRoles - userRoles
				when (missingRoles.isEmpty()) {
					true  -> Mono.just(it)
					false -> Mono.error<Exception>(RuntimeException("Missing roles $missingRoles"))
				}
			}
			.flatMap {
				environment
					.getDataLoader<Long, List<WorkerDto>>("workerLoader")
					.load(productionId)
					.toMono()
			}.toFuture()
	}

	private fun createGraphQLError(environment: DataFetchingEnvironment): DataFetcherResult<Any> {
		val error = SimpleKotlinGraphQLError(
			RuntimeException("Role required"),
			listOf(environment.field.sourceLocation),
			environment.executionStepInfo.path.toList()
		)
		return DataFetcherResult.newResult<Any>()
			.error(error)
			.build()
	}
}
