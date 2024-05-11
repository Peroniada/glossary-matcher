package com.sperek.file.api.kotlinxio.filewriter

import com.sperek.file.api.FileWriter
import com.sperek.file.api.Path
import com.sperek.file.api.SaveResult
import com.sperek.file.api.Markdown
import com.sperek.file.api.kotlinxio.KotlinxIOFileSystem


private const val PATH_IS_NOT_INITIALIZED = "Path is not initialized"

private const val CONTENT_IS_NOT_INITIALIZED = "Content is not initialized"

private const val NAME_IS_NOT_INITIALIZED = "Name is not initialized"

private const val EXTENSION_IS_NOT_INITIALIZED = "Extension is not initialized"

class KotlinXIOFileWriter(private val kotlinxIOFileSystem: KotlinxIOFileSystem) : FileWriter {
    private lateinit var path: Path
    private lateinit var name: String
    private lateinit var extension: String
    private lateinit var content: String

    override fun into(path: Path): FileWriter {
        this.path = path
        return this
    }

    override fun withName(name: String): FileWriter {
        this.name = name
        return this
    }

    override fun withExtension(extension: String): FileWriter {
        this.extension = extension
        return this
    }

    override fun withContent(content: String): FileWriter {
        this.content = content
        return this
    }

    override fun empty(): FileWriter {
        this.content = ""
        return this
    }

    override fun <T : Markdown> fromObject(obj: T): FileWriter {
        this.content = obj.toMarkdown()
        return this
    }


    override fun save(): SaveResult {
        check(::path.isInitialized) { PATH_IS_NOT_INITIALIZED }
        if (this.path.isDirectory()) {
            return createAFileFromDirectoryPath()
        }
        return createAFileFromPathToFile()
    }

    private fun createAFileFromPathToFile(): SaveResult {
        check(::content.isInitialized) { CONTENT_IS_NOT_INITIALIZED }
        return kotlinxIOFileSystem.saveFile(this.path, this.content)
    }

    private fun createAFileFromDirectoryPath(): SaveResult {
        check(::content.isInitialized) { CONTENT_IS_NOT_INITIALIZED }
        val newFilePath = getNewFilePath()
        return kotlinxIOFileSystem.saveFile(newFilePath, content)
    }

    private fun getNewFilePath(): Path {
        check(::name.isInitialized) { NAME_IS_NOT_INITIALIZED }

        if(::extension.isInitialized) {
            return this.path.append(name).append(extension, Path.EXTENSION)
        }
        return this.path.append(name)
    }

}

