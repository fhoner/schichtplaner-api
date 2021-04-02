package com.felixhoner.schichtplaner.api.business.service

import com.felixhoner.schichtplaner.api.business.model.Plan
import com.felixhoner.schichtplaner.api.business.model.Production
import com.felixhoner.schichtplaner.api.business.model.Shift
import com.felixhoner.schichtplaner.api.business.model.User
import com.felixhoner.schichtplaner.api.business.model.Worker
import com.felixhoner.schichtplaner.api.graphql.dto.UserRoleDto
import com.felixhoner.schichtplaner.api.persistence.entity.PlanEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserEntity
import com.felixhoner.schichtplaner.api.persistence.entity.UserRole
import com.felixhoner.schichtplaner.api.persistence.entity.WorkerEntity
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalTime.parse
import java.util.*

class TransformerBoTest {
    private val cut = TransformerBo()

    private val uuid: UUID = UUID.fromString("a4235dee-ccb0-4577-82ca-318c0ea5eca4")

    @Test
    fun `should transform plan`() {
        val planEntity = PlanEntity(
            id = 4711,
            name = "Plan",
            uuid = uuid
        )
        val plan = Plan(
            id = 4711,
            name = "Plan",
            uuid = uuid
        )
        cut.toBo(planEntity) shouldBe plan
    }

    @Test
    fun `should transform production`() {
        val productionEntity = ProductionEntity(
            id = 4711,
            uuid = uuid,
            name = "Production",
            plan = mockk()
        )
        val production = Production(
            id = 4711,
            uuid = uuid,
            name = "Production"
        )
        cut.toBo(productionEntity) shouldBe production
    }

    @Test
    fun `should transform shift`() {
        val shiftEntity = ShiftEntity(
            id = 4711,
            uuid = uuid,
            startTime = parse("14:00"),
            endTime = parse("16:00"),
        )
        val shift = Shift(
            id = 4711,
            uuid = uuid,
            startTime = parse("14:00"),
            endTime = parse("16:00")
        )
        cut.toBo(shiftEntity) shouldBe shift
    }

    @Test
    fun `should transform worker`() {
        val workerEntity = WorkerEntity(
            id = 4711,
            uuid = uuid,
            firstname = "Max",
            lastname = "Mustermann",
            email = "max@mustermann",
            shifts = mutableListOf(
                ShiftEntity(id = 12, startTime = parse("14:00"), endTime = parse("15:00")),
                ShiftEntity(id = 13, startTime = parse("15:00"), endTime = parse("16:00")),
            )
        )
        val worker = Worker(
            id = 4711,
            uuid = uuid,
            firstname = "Max",
            lastname = "Mustermann",
            email = "max@mustermann",
            shiftIds = listOf(12, 13)
        )
        cut.toBo(workerEntity) shouldBe worker
    }

    @Test
    fun `should transform user`() {
        val userEntity = UserEntity(
            id = 4711,
            uuid = uuid,
            email = "max@mustermann",
            password = "pw",
            role = UserRole.READER
        )
        val user = User(
            id = 4711,
            uuid = uuid,
            email = "max@mustermann",
            password = "pw",
            role = UserRole.READER
        )
        cut.toBo(userEntity) shouldBe user
    }

    @Nested
    inner class UserRoleMapping {

        @Test
        fun `should transform reader`() {
            cut.toBo(UserRoleDto.READER) shouldBe UserRole.READER
        }

        @Test
        fun `should transform writer`() {
            cut.toBo(UserRoleDto.WRITER) shouldBe UserRole.WRITER
        }

        @Test
        fun `should transform admin`() {
            cut.toBo(UserRoleDto.ADMIN) shouldBe UserRole.ADMIN
        }

    }
}
