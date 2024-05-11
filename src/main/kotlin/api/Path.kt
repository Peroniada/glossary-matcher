package com.sperek.file.api

/**
 * Represents a file path.
 *
 * @property path The string representation of the file path.
 * @constructor Creates a Path with the given path.
 */
data class Path(val path: String) {
    fun resolve(path: Path): Path {
        return if (this.isDirectory()) {
            Path(this.path + path.path)
        } else {
            Path(this.path + "/" + path.path)
        }
    }
    fun isEmpty() = path.isEmpty()
    fun asString() = path
    fun getFileName() = if (isDirectory()) {
        path.substringBeforeLast("/").substringAfterLast("/")
    } else {
        path.substringAfterLast("/")
    }

    fun isFile() = !isDirectory()

    fun containsFileExtension() = path.contains(fileExtensionRegex)

    fun getParent() = if (isDirectory()) {
        Path(path.substringBeforeLast("/").substringBeforeLast("/"))
    } else {
        Path(path.substringBeforeLast("/"))
    }

    fun getExtension() = path.substringAfterLast(".")
    fun justName() = this.getFileName().substringBeforeLast(".")
    fun isDirectory() = path.endsWith("/")

    fun append(segment: String, delimiter: String = EMPTY): Path {
        if (delimiter.isEmpty()) {
            return Path("$path$segment")
        }
        return Path("$path$delimiter$segment")
    }

    fun rename(fileNewName: String): Path {
        val parent = getParent().asString()
        return Path("$parent/$fileNewName")
    }

    companion object Delimiters {
        const val DIRECTORY = "/"
        const val EXTENSION = "."
        const val EMPTY = ""
        val fileExtensionRegex = Regex("\\.[a-zA-Z0-9]+")
    }
}