package com.sperek.file.api.kotlinxio.delete

import com.sperek.file.api.operation.DeleteOperation
import com.sperek.file.api.operation.DeleteResult
import com.sperek.file.api.Path
import com.sperek.file.api.kotlinxio.KotlinxIOFileSystem

private const val PATH_IS_NOT_INITIALIZED = "Path is not initialized"

class KotlinXIODeleteOperation(private val kotlinxIOFileSystem: KotlinxIOFileSystem) : DeleteOperation {

    private lateinit var path: Path

    override fun file(path: Path): DeleteOperation {
        this.path = path
        return this
    }

    override fun perform(): DeleteResult {
        check(::path.isInitialized) { PATH_IS_NOT_INITIALIZED }
        return kotlinxIOFileSystem.delete(this.path)
    }
}