package com.sperek.file.api

import com.sperek.file.api.operation.DeleteOperation
import com.sperek.file.api.operation.MoveOperation
import com.sperek.file.api.operation.RenameOperation
import com.sperek.file.api.search.*

/**
 * Interface for performing file operations and searching files.
 */
interface FilesApi {
    fun fileOperation(): FileOperationBuilder
    fun search(): SearchBuilder
}


/**
 * This interface represents a builder for file operations.
 *
 * It provides methods to create, read, delete, rename, and move files.
 * Each method returns an interface specific to the operation, which allows further configuration or execution of the operation.
 *
 * Example usage:
 * ``` kotlin
 * val fileOperationBuilder: FileOperationBuilder =*/
interface FileOperationBuilder {
    fun create(): FileWriter
    fun read(): FileReader
    fun delete(): DeleteOperation
    fun rename(): RenameOperation
    fun move(): MoveOperation
}

/**
 * Represents a FileReader interface that provides methods for reading file content and mapping it to a specific type.
 */
interface FileReader {
    fun from(path: Path): FileReader
    fun getFileContent(): String
    fun <T> mapTo(mapper: (String) -> T): T
}

/**
 * A utility class for writing files to disk.
 *
 * You can use this class to create, modify, or overwrite files with content.
 * The methods in this class provide a fluent API for chaining multiple operations
 * together.
 */
interface FileWriter {
    fun into(path: Path): FileWriter
    fun withName(name: String): FileWriter
    fun withExtension(extension: String): FileWriter
    fun withContent(content: String): FileWriter
    fun empty(): FileWriter
    fun <T : Markdown> fromObject(obj: T): FileWriter
    fun save(): SaveResult
}

/**
 * Represents the result of a save operation.
 *
 * @property success Indicates whether the save operation was successful or not.
 * @property path The path where the saved file is located.
 */
data class SaveResult(val success: Boolean, val path: Path)

/**
 * The `Stringify` interface defines a single method, `stringify()`, which is used to convert an object to a string representation.
 * Classes that implement this interface should provide their own implementation of the `stringify()` method.
 */
fun interface Markdown {
    fun toMarkdown(): String
}

/**
 * Provides methods for building different types of search operations.
 */
interface SearchBuilder {
    fun simpleSearch(): SimpleSearch
    fun criteriaSearch(): CriteriaSearch
    fun specificationSearch(): SpecificationSearch
}
