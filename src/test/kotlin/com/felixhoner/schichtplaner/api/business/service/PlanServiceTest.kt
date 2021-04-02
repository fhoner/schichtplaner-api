package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Plan
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.repository.PlanRepository
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import java.util.*

class PlanServiceTest {

    private val planRepository: PlanRepository = mockk()
    private val transformer: TransformerBo = mockk()

    private val cut = PlanService(
        planRepository,
        transformer
    )

    @Test
    fun `should get all plans`() {
        val planEntities = listOf(
            PlanEntity(name = "Plan 1"),
            PlanEntity(name = "Plan 2")
        )
        val planBos = listOf(
            Plan(id = 1, uuid = UUID.randomUUID(), name = "Plan 1"),
            Plan(id = 2, uuid = UUID.randomUUID(), name = "Plan 2")
        )
        every { planRepository.findAll() } returns planEntities
        every { transformer.toBo(planEntities[0]) } returns planBos[0]
        every { transformer.toBo(planEntities[1]) } returns planBos[1]

        val result = cut.getAll()
        result shouldHaveSize 2
        result shouldContainExactlyInAnyOrder planBos
    }

}
