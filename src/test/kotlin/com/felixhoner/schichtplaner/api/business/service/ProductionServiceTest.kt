package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Production
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.repository.ProductionRepository
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.*

class ProductionServiceTest {

    private val productionRepository: ProductionRepository = mockk()
    private val transformer: TransformerBo = mockk()

    private val cut = ProductionService(
        productionRepository,
        transformer
    )

    @Test
    fun `get by plans result list should be built correctly`() {
        val planEntities = listOf(
            PlanEntity(id = 1, name = "Plan1"),
            PlanEntity(id = 2, name = "Plan2"),
        )
        val productionEntities = listOf(
            ProductionEntity(name = "Prod1", plan = planEntities[0]),
            ProductionEntity(name = "Prod2", plan = planEntities[0]),
            ProductionEntity(name = "Prod3", plan = planEntities[1]),
        )
        val productionBos = listOf(
            Production(id = 1, name = "Prod1", uuid = UUID.randomUUID()),
            Production(id = 2, name = "Prod2", uuid = UUID.randomUUID()),
            Production(id = 3, name = "Prod3", uuid = UUID.randomUUID()),
        )
        every { productionRepository.findAllByPlanIds(any()) } returns productionEntities
        every { transformer.toBo(productionEntities[0]) } returns productionBos[0]
        every { transformer.toBo(productionEntities[1]) } returns productionBos[1]
        every { transformer.toBo(productionEntities[2]) } returns productionBos[2]

        val result = cut.getByPlans(listOf(8, 2, 1, 9))
        result shouldHaveSize 4
        result[0] shouldBe emptyList()
        result[1] shouldBe listOf(productionBos[2])
        result[2] shouldBe listOf(productionBos[0], productionBos[1])
        result[3] shouldBe emptyList()
    }

}
