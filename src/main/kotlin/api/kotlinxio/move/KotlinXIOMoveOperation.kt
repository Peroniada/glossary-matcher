package com.sperek.file.api.kotlinxio.move

import com.sperek.file.api.operation.MoveOperation
import com.sperek.file.api.operation.MoveResult
import com.sperek.file.api.Path
import com.sperek.file.api.kotlinxio.KotlinxIOFileSystem

private const val PATH_IS_NOT_INITIALIZED = "Path is not initialized"

private const val DESTINATION_IS_NOT_INITIALIZED = "Destination is not initialized"

class KotlinXIOMoveOperation(private val kotlinxIOFileSystem: KotlinxIOFileSystem) : MoveOperation {

    private lateinit var path: Path
    private lateinit var destination: Path


    override fun file(path: Path): MoveOperation {
        this.path = path
        return this
    }

    override fun to(destination: Path): MoveOperation {
        this.destination = destination
        return this
    }

    override fun perform(): MoveResult {
        check(::path.isInitialized) { PATH_IS_NOT_INITIALIZED }
        check(::destination.isInitialized) { DESTINATION_IS_NOT_INITIALIZED }
        return kotlinxIOFileSystem.move(this.path, this.destination)
    }
}