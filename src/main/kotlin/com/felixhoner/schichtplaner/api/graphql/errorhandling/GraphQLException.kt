package com.felixhoner.schichtplaner.api.graphql.errorhandling

import com.felixhoner.schichtplaner.api.business.exception.ErrorDefinition

/**
 * Custom exception base class to be used within data fetchers.
 * The exception will be caught and transformed by [CustomDataFetcherExceptionHandler].
 */
open class GraphQLException(
	val error: ErrorDefinition,
	exception: Throwable
): Exception(error.message, exception)
