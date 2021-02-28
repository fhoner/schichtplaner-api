package com.felixhoner.schichtplaner.api.graphql.errorhandling

import com.expediagroup.graphql.spring.exception.SimpleKotlinGraphQLError
import com.felixhoner.schichtplaner.api.business.exception.ErrorCodes
import graphql.ErrorClassification
import graphql.ErrorType
import graphql.GraphQLError
import graphql.execution.DataFetcherExceptionHandler
import graphql.execution.DataFetcherExceptionHandlerParameters
import graphql.execution.DataFetcherExceptionHandlerResult
import graphql.language.SourceLocation
import mu.KotlinLogging

/**
 * Extending the [SimpleKotlinGraphQLError] to provide the extensions directly.
 */
class ExtendedKotlinGraphQLError(
    exception: Throwable,
    locations: List<SourceLocation>? = null,
    path: List<Any>? = null,
    private val extensions: Map<String, Any> = emptyMap(),
    errorType: ErrorClassification = ErrorType.DataFetchingException
) : SimpleKotlinGraphQLError(exception, locations, path, errorType) {

    override fun getExtensions(): Map<String, Any> = extensions

}

/**
 * Custom exception handler that will transform [GraphQLException] into
 * extensions properly. They are then part of the extensions object in the
 * result json.
 */
class CustomDataFetcherExceptionHandler : DataFetcherExceptionHandler {

    private val logger = KotlinLogging.logger {}

    override fun onException(handlerParameters: DataFetcherExceptionHandlerParameters): DataFetcherExceptionHandlerResult {
        val exception = handlerParameters.exception
        val sourceLocation = handlerParameters.sourceLocation
        val path = handlerParameters.path
        val extensions = when (exception) {
            is GraphQLException -> exception.error.toMap()
            else -> ErrorCodes.UNEXPECTED_ERROR.toMap()
        }
        logger.warn("Exception caught in data fetcher: $exception")
        logger.info { exception.stackTraceToString() }
        logger.warn("Extensions: $extensions")
        val error: GraphQLError = ExtendedKotlinGraphQLError(
            exception = exception,
            locations = listOf(sourceLocation),
            path = path.toList(),
            extensions = extensions
        )
        logger.warn { "Returning error" }
        return DataFetcherExceptionHandlerResult.newResult().error(error).build()
    }

}
