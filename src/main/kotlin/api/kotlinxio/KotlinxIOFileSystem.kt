package com.sperek.file.api.kotlinxio

import com.sperek.file.api.*
import com.sperek.file.api.operation.DeleteResult
import com.sperek.file.api.operation.MoveResult
import com.sperek.file.api.operation.RenameResult
import kotlinx.io.Buffer
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.io.files.Path as KPath

class KotlinxIOFileSystem {
    private val fileSystem = SystemFileSystem

    fun exists(path: Path): Boolean {
        return fileSystem.exists(path.toKPath())
    }

    fun getAbsolutePath(path: Path): Path {
        val absolutePath = fileSystem.resolve(path.toKPath())
        return Path(absolutePath.toString())
    }

    fun delete(path: Path): DeleteResult {
        if (!exists(path)) {
            return DeleteResult(false)
        }
        fileSystem.delete(path.toKPath())
        return DeleteResult(true)
    }

    fun createDirectories(path: Path): Boolean {
        if (exists(path)) {
            return false
        }
        fileSystem.createDirectories(path.toKPath())
        return true
    }

    fun move(source: Path, target: Path): MoveResult {
        if (!exists(source)) {
            return MoveResult(source, false)
        }
        fileSystem.atomicMove(source.toKPath(), target.toKPath())
        return MoveResult(target, true)
    }

    fun rename(path: Path, newName: String): RenameResult {
        if (!exists(path)) {
            return RenameResult(path, false)
        }
        val sourcePath = path.toKPath()
        val newPathDirectory = path.rename(newName).toKPath()
        fileSystem.atomicMove(sourcePath, newPathDirectory)
        return RenameResult(newPathDirectory.toPath(), true)
    }

    fun saveFile(pathToFile: Path, content: String, override: Boolean = true): SaveResult {
        if (exists(pathToFile) && override.not()) {
            return SaveResult(false, pathToFile)
        }
        val directoryPath = pathToFile.getParent()
        if (!exists(directoryPath)) {
            createDirectories(directoryPath)
        }

        val buffer = Buffer()
        buffer.writeString(content)

        fileSystem.sink(pathToFile.toKPath()).use { sink ->
            sink.write(buffer, buffer.size)
        }

        return SaveResult(true, pathToFile)
    }

    fun readFileAsString(path: Path): String {
        val buffer = Buffer()
        val source = fileSystem.source(path.toKPath())
        while (source.readAtMostTo(buffer, Long.MAX_VALUE) != -1L) {
            // Loop until all data is read
        }

        return buffer.readString()
    }

    private fun Path.toKPath(): KPath {
        return KPath(this.asString())
    }

    private fun KPath.toPath(): Path {
        return Path(this.toString())
    }
}