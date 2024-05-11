package com.sperek.file.api.kotlinxio.filereader

import com.sperek.file.api.FileReader
import com.sperek.file.api.Path
import com.sperek.file.api.kotlinxio.KotlinxIOFileSystem

private const val PATH_IS_NOT_INITIALIZED = "Path is not initialized"
private const val PATH_IS_A_DIRECTORY = "Path is a directory"

class KotlinXIOFileReader(private val kotlinxIOFileSystem: KotlinxIOFileSystem) : FileReader {
    private lateinit var path: Path
    override fun from(path: Path): FileReader {
        this.path = path
        return this
    }

    override fun getFileContent(): String {
        check(::path.isInitialized) { PATH_IS_NOT_INITIALIZED }
        check(!path.isDirectory()) { PATH_IS_A_DIRECTORY }
        val fileContentString = kotlinxIOFileSystem.readFileAsString(this.path)
        return fileContentString
    }

    override fun <T> mapTo(mapper: (String) -> T): T {
        val fileContentString = getFileContent()
        return mapper(fileContentString)
    }
}