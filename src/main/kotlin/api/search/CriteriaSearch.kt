package com.sperek.file.api.search

import com.sperek.file.api.Path

/**
 * This interface represents a criteria-based search for files in a given directory or set of directories.
 */
interface CriteriaSearch {
    fun inPath(path: Path): CriteriaSearch
    fun inPaths(paths: Set<Path>): CriteriaSearch
    fun withExtension(extension: String): CriteriaSearch
    fun withName(name: String): CriteriaSearch
    fun withContent(content: String): CriteriaSearch
    fun traversal(): CriteriaSearch
    fun execute(): Sequence<Path>
}

/**
 * Represents the search criteria used to filter files during a search operation.
 *
 * @property extension The file extension to filter by.
 * @property name The file name to filter by.
 * @property content The content to search for in the file.
 */
data class SearchCriteria(var extension: String = "", var name: String = "", var content: String = "")