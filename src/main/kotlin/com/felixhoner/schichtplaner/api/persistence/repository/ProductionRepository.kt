package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.entity.ProductionEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ProductionRepository : CrudRepository<ProductionEntity, Long> {

    /** Gets all plans whose id is in the parameter list. */
    @Query("select p from ProductionEntity p where p.plan.id in :planIds")
    fun findAllByPlanIds(planIds: List<Long>): List<ProductionEntity>

    /** Gets the production by its uuid if one exists. Shifts will be fetched along ordered by their startTime. */
    @Query("select p from ProductionEntity p left join fetch p.shifts s where p.uuid = :productionUuid order by s.startTime")
    fun findByUuid(productionUuid: UUID): ProductionEntity?

}
