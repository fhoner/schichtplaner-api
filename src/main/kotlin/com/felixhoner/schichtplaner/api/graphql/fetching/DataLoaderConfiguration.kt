package com.felixhoner.schichtplaner.api.graphql.fetching

import com.expediagroup.graphql.spring.execution.DataLoaderRegistryFactory
import com.felixhoner.schichtplaner.api.graphql.dto.ProductionDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import com.felixhoner.schichtplaner.api.service.ProductionService
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.concurrent.CompletableFuture

@Configuration
class DataLoaderConfiguration {

	@Bean
	fun dataLoaderRegistryFactory(service: ProductionService, transformer: TransformerDto): DataLoaderRegistryFactory {
		return object: DataLoaderRegistryFactory {
			override fun generate(): DataLoaderRegistry {
				val registry = DataLoaderRegistry()
				val productionLoader = DataLoader<Long, List<ProductionDto>> { ids ->
					CompletableFuture.supplyAsync {
						service.getByPlans(ids)
							.map {
								it.map { production -> transformer.toDto(production) }
							}
					}
				}
				registry.register("productionLoader", productionLoader)
				return registry
			}
		}
	}
}
