package com.felixhoner.schichtplaner.api.graphql.query

import com.expediagroup.graphql.server.operations.Query
import com.felixhoner.schichtplaner.api.business.service.PlanService
import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import com.felixhoner.schichtplaner.api.graphql.dto.PlanDto
import com.felixhoner.schichtplaner.api.graphql.dto.ProductionDto
import com.felixhoner.schichtplaner.api.graphql.dto.ShiftDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import com.felixhoner.schichtplaner.api.graphql.dto.WorkerDto
import com.felixhoner.schichtplaner.api.graphql.execution.GraphQLSecurityContext
import graphql.schema.DataFetcher
import graphql.schema.DataFetchingEnvironment
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import reactor.kotlin.core.publisher.toMono
import java.util.concurrent.CompletableFuture

@Component
@Scope("prototype")
@Suppress("unused")
class PlanQuery(
    private val planService: PlanService,
    private val transformer: TransformerDto
) : Query {

    @Authorized("READER")
    fun getPlans() = planService.getAll().map(transformer::toDto)

}

@Component("[ProductionDto!]DataFetcher")
@Scope("prototype")
class ProductionsDataFetcher : DataFetcher<CompletableFuture<List<ProductionDto>>>, BeanFactoryAware {

    private lateinit var beanFactory: BeanFactory

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<ProductionDto>> {
        val planId = environment.getSource<PlanDto>().id
        return environment
            .getDataLoader<Long, List<ProductionDto>>("productionLoader")
            .clearAll()
            .load(planId)
    }
}

@Component("[ShiftDto!]DataFetcher")
@Scope("prototype")
class ShiftsDataFetcher : DataFetcher<CompletableFuture<List<ShiftDto>>>, BeanFactoryAware {

    private lateinit var beanFactory: BeanFactory

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<ShiftDto>> {
        val productionId = environment.getSource<ProductionDto>().id
        return environment
            .getDataLoader<Long, List<ShiftDto>>("shiftLoader")
            .clearAll()
            .load(productionId)
    }
}

@Component("[WorkerDto!]DataFetcher")
@Scope("prototype")
class WorkersDataFetcher : DataFetcher<CompletableFuture<List<WorkerDto>>>, BeanFactoryAware {

    private lateinit var beanFactory: BeanFactory

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.beanFactory = beanFactory
    }

    override fun get(environment: DataFetchingEnvironment): CompletableFuture<List<WorkerDto>> {
        val productionId = environment.getSource<ShiftDto>().id
        val context = environment.getContext<GraphQLSecurityContext>()
        return context.securityContext
            .flatMap {
                environment
                    .getDataLoader<Long, List<WorkerDto>>("workerLoader")
                    .clearAll()
                    .load(productionId)
                    .toMono()
            }.toFuture()
    }
}
