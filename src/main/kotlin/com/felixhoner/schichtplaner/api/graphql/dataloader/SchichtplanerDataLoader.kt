package com.felixhoner.schichtplaner.api.graphql.dataloader

import com.felixhoner.schichtplaner.api.graphql.query.DataLoaderConfiguration
import org.dataloader.DataLoader

/**
 * Interface that every dataloader has to implement in order to be registered successfully
 * in the [DataLoaderConfiguration].
 */
interface SchichtplanerDataLoader {

    val name: String

    fun getInstance(): DataLoader<*, *>

}
