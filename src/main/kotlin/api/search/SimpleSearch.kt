package com.sperek.file.api.search

import com.sperek.file.api.Path

/**
 * Represents a simple search functionality.
 * Provides methods to search for files within a specified path or set of paths,
 * as well as traverse all files within a specified path.
 */
interface SimpleSearch {
    fun fileExists(path: Path): Boolean
    fun filesIn(path: Path): Sequence<Path>
    fun filesIn(paths: Set<Path>): Sequence<Path>
    fun traverseAllIn(path: Path): Sequence<Path>
}