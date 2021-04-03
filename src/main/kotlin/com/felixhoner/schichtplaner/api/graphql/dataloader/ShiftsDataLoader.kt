package com.felixhoner.schichtplaner.api.graphql.dataloader

import com.felixhoner.schichtplaner.api.business.service.ShiftService
import com.felixhoner.schichtplaner.api.graphql.dto.ShiftDto
import com.felixhoner.schichtplaner.api.graphql.dto.TransformerDto
import org.dataloader.DataLoader
import org.springframework.stereotype.Component
import java.util.concurrent.CompletableFuture

@Component
class ShiftsDataLoader(
    private val shiftService: ShiftService,
    private val transformer: TransformerDto
) : SchichtplanerDataLoader {

    override val name: String get() = "shiftLoader"

    override fun getInstance(): DataLoader<*, *> = DataLoader<Long, List<ShiftDto>> { ids ->
        CompletableFuture.supplyAsync {
            shiftService.getByProductions(ids)
                .map {
                    it.map { shift -> transformer.toDto(shift) }
                }
        }
    }

}
