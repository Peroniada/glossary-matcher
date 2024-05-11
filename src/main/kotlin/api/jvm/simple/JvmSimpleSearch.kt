package com.sperek.file.api.jvm.simple

import com.sperek.file.api.Path
import com.sperek.file.api.search.SimpleSearch
import com.sperek.file.api.jvm.JvmDirectoryLookup

class JvmSimpleSearch(private val jvmDirectoryLookup: JvmDirectoryLookup) : SimpleSearch {
    override fun fileExists(path: Path): Boolean {
        return jvmDirectoryLookup.exists(path)
    }


    override fun filesIn(path: Path): Sequence<Path> {
        return searchFilesDirectory(path)
    }

    override fun filesIn(paths: Set<Path>): Sequence<Path> {
        return paths.asSequence().flatMap { searchFilesDirectory(it) }
    }

    override fun traverseAllIn(path: Path): Sequence<Path> {
        return pathTraversal(path)
    }

    private fun searchFilesDirectory(path: Path): Sequence<Path> {
        val (_, files) = jvmDirectoryLookup.getDirectoryContent(path)

        return files.asSequence()
    }

    private fun pathTraversal(path: Path): Sequence<Path> {
        val directory = jvmDirectoryLookup.getDirectory(path)
        return directory.getAllFilesTraversal().asSequence()
    }
}

