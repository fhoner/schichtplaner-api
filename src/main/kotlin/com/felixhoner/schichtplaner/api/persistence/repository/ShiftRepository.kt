package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.entity.ShiftEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ShiftRepository: CrudRepository<ShiftEntity, Long> {

	@Query("select s from ShiftEntity s where s.production.id in :productionIds")
	fun findAllByProductionIds(productionIds: List<Long>): List<ShiftEntity>

}
