package com.sperek.file.api.kotlinxio.rename

import com.sperek.file.api.Path
import com.sperek.file.api.operation.RenameOperation
import com.sperek.file.api.operation.RenameResult
import com.sperek.file.api.kotlinxio.KotlinxIOFileSystem

private const val NEW_NAME_IS_NOT_INITIALIZED = "New name is not initialized"
private const val PATH_IS_NOT_INITIALIZED = "Path is not initialized"

class KotlinXIORenameOperation(private val kotlinxIOFileSystem: KotlinxIOFileSystem) : RenameOperation {

    private lateinit var path: Path
    private lateinit var newName: String

    override fun file(path: Path): RenameOperation {
        this.path = path
        return this
    }

    override fun to(newName: String): RenameOperation {
        this.newName = newName
        return this
    }

    override fun perform(): RenameResult {
        check(::path.isInitialized) { PATH_IS_NOT_INITIALIZED }
        check(::newName.isInitialized) { NEW_NAME_IS_NOT_INITIALIZED }
        return kotlinxIOFileSystem.rename(this.path, this.newName)
    }
}