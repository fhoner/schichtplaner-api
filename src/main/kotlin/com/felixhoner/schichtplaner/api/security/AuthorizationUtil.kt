package com.felixhoner.schichtplaner.api.security

import com.felixhoner.schichtplaner.api.graphql.directive.Authorized
import graphql.execution.MergedField
import graphql.language.Field
import org.springframework.security.core.context.SecurityContext
import reactor.core.publisher.Mono
import kotlin.reflect.KClass
import kotlin.reflect.full.declaredMemberProperties

object AuthorizationUtil {

	fun <T: Any> assertRolesOnSelectionSet(context: SecurityContext, mergedField: MergedField, type: KClass<T>): Mono<Boolean> {
		val fields: List<String> = mergedField.fields[0].selectionSet.selections.map { (it as Field).name }
		val annotated: List<Annotation> = type.declaredMemberProperties
			.filter { fields.contains(it.name) }
			.flatMap { it.annotations }
		val requiredRoles: List<String> = annotated.filterIsInstance<Authorized>().flatMap { it.roles.toList() }
		val userRoles: List<String> = context.authentication.authorities.map { au -> au.authority }
		if ((requiredRoles - userRoles).isNotEmpty()) {
			return Mono.error(RuntimeException("Roles $requiredRoles are required"))
		}
		return Mono.just(true)
	}

}
