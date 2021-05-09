package com.felixhoner.schichtplaner.api.graphql.query

import com.expediagroup.graphql.server.execution.DataLoaderRegistryFactory
import com.felixhoner.schichtplaner.api.graphql.dataloader.SchichtplanerDataLoader
import com.felixhoner.schichtplaner.api.graphql.dto.ProductionDto
import com.felixhoner.schichtplaner.api.graphql.dto.ShiftDto
import com.felixhoner.schichtplaner.api.graphql.dto.WorkerDto
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class DataLoaderConfiguration(
    private val registeredDataLoaders: List<SchichtplanerDataLoader>
) {
    @Bean
    fun dataLoaderRegistryFactory(): DataLoaderRegistryFactory {
        return object : DataLoaderRegistryFactory {
            override fun generate(): DataLoaderRegistry = DataLoaderRegistry().apply {
                register("productionLoader", getLoader<Long, List<ProductionDto>>("productionLoader"))
                register("shiftLoader", getLoader<Long, List<ShiftDto>>("shiftLoader"))
                register("workerLoader", getLoader<Long, List<WorkerDto>>("workerLoader"))
            }
        }
    }

    /**
     * Looks for the searched dataloader by the given name in all implementations of [SchichtplanerDataLoader].
     */
    @Suppress("unchecked_cast")
    private fun <T, V> getLoader(name: String) = this.registeredDataLoaders
        .find { it.name == name }
        ?.let { it.getInstance() as DataLoader<T, V> }
        ?: throw RuntimeException(
            "DataLoader with name [$name] was not registered as an implementation of [${SchichtplanerDataLoader::name}]"
        )

}
