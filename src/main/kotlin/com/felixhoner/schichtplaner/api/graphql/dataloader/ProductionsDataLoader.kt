package com.felixhoner.schichtplaner.api.graphql.dataloader

import com.felixhoner.schichtplaner.api.business.service.ProductionService
import com.felixhoner.schichtplaner.api.graphql.dto.ProductionDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import org.dataloader.DataLoader
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class ProductionsDataLoader(
    private val productionService: ProductionService,
    private val transformer: TransformerDto
) : SchichtplanerDataLoader {

    override val name: String get() = "productionLoader"

    override fun getInstance(): DataLoader<*, *> = DataLoader<Long, List<ProductionDto>> { ids ->
        CompletableFuture.supplyAsync {
            productionService.getByPlans(ids)
                .map {
                    it.map { production -> transformer.toDto(production) }
                }
        }
    }
}
