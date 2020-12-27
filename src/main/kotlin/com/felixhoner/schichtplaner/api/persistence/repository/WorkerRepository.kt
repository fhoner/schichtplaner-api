package com.felixhoner.schichtplaner.api.persistence.repository

import com.felixhoner.schichtplaner.api.persistence.entity.WorkerEntity
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository

interface WorkerRepository: CrudRepository<WorkerEntity, Long> {

	@Query("select w from WorkerEntity w inner join fetch w.shifts s where s.id in :shiftIds")
	fun findAllByShiftIds(shiftIds: List<Long>): List<WorkerEntity>

}
