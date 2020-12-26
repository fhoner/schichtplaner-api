package com.felixhoner.schichtplaner.api.graphql.dto

data class ShiftDto(
	val id: Long,
	val uuid: String,
	val startTime: String,
	val endTime: String
)
