package com.felixhoner.schichtplaner.api.graphql.query

import com.expediagroup.graphql.spring.execution.DataLoaderRegistryFactory
import com.felixhoner.schichtplaner.api.business.service.ProductionService
import com.felixhoner.schichtplaner.api.business.service.ShiftService
import com.felixhoner.schichtplaner.api.business.service.WorkerService
import com.felixhoner.schichtplaner.api.graphql.dto.ProductionDto
import com.felixhoner.schichtplaner.api.graphql.dto.ShiftDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import com.felixhoner.schichtplaner.api.graphql.dto.WorkerDto
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CompletableFuture

@Configuration
class DataLoaderConfiguration(
    private val productionService: ProductionService,
    private val shiftService: ShiftService,
    private val workerService: WorkerService,
    private val transformer: TransformerDto
) {
    @Bean
    fun dataLoaderRegistryFactory(): DataLoaderRegistryFactory {
        return object : DataLoaderRegistryFactory {
            override fun generate(): DataLoaderRegistry = DataLoaderRegistry().apply {
                register("productionLoader", productionLoader)
                register("shiftLoader", shiftLoader)
                register("workerLoader", workerLoader)
            }
        }
    }

    private val productionLoader = DataLoader<Long, List<ProductionDto>> { ids ->
        CompletableFuture.supplyAsync {
            productionService.getByPlans(ids)
                .map {
                    it.map { production -> transformer.toDto(production) }
                }
        }
    }

    private val shiftLoader = DataLoader<Long, List<ShiftDto>> { ids ->
        CompletableFuture.supplyAsync {
            shiftService.getByProductions(ids)
                .map {
                    it.map { shift -> transformer.toDto(shift) }
                }
        }
    }

    private val workerLoader = DataLoader<Long, List<WorkerDto>> { ids ->
        CompletableFuture.supplyAsync {
            val result = ids.map { Pair(it, mutableSetOf<WorkerDto>()) }.toMap()
            val allWorkers = workerService.findAllByShift(ids)
            result.forEach { m ->
                val matched = allWorkers
                    .filter { worker -> worker.shiftIds.any { it == m.key } }
                    .map(transformer::toDto)
                m.value.addAll(matched)
            }
            result.values.map { it.toList() }
        }
    }
}
