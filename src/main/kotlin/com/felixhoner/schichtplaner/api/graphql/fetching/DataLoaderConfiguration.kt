package com.felixhoner.schichtplaner.api.graphql.fetching

import com.expediagroup.graphql.spring.execution.DataLoaderRegistryFactory
import com.felixhoner.schichtplaner.api.graphql.dto.*
import com.felixhoner.schichtplaner.api.service.ProductionService
import com.felixhoner.schichtplaner.api.service.ShiftService
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CompletableFuture

@Configuration
class DataLoaderConfiguration(
	private val productionService: ProductionService,
	private val shiftService: ShiftService,
	private val transformer: TransformerDto
) {
	@Bean
	fun dataLoaderRegistryFactory(): DataLoaderRegistryFactory {
		return object: DataLoaderRegistryFactory {
			override fun generate(): DataLoaderRegistry = DataLoaderRegistry().apply {
				register("productionLoader", productionLoader)
				register("shiftLoader", shiftLoader)
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
}
