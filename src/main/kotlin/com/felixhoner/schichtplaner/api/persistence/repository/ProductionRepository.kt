package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ProductionRepository : CrudRepository<ProductionEntity, Long> {

    @Query("select p from ProductionEntity p where p.plan.id in :planIds")
    fun findAllByPlanIds(planIds: List<Long>): List<ProductionEntity>

    fun findByUuid(planUuid: UUID): ProductionEntity?

}
