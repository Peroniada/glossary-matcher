package com.sperek.file.api.jvm.criteria

import com.sperek.file.api.search.CriteriaSearch
import com.sperek.file.api.FileReader
import com.sperek.file.api.Path
import com.sperek.file.api.search.SearchCriteria
import com.sperek.file.api.jvm.JvmDirectoryLookup

class JvmCriteriaSearch(
    private val jvmDirectoryLookup: JvmDirectoryLookup,
    private val fileReader: FileReader
) : CriteriaSearch {
    private var traversalSearch: Boolean = false
    private val paths = mutableSetOf<Path>()
    private val searchCriteria = SearchCriteria()

    override fun inPath(path: Path): CriteriaSearch {
        paths.add(path)
        return this
    }

    override fun inPaths(paths: Set<Path>): CriteriaSearch {
        this.paths.addAll(paths)
        return this
    }

    override fun withExtension(extension: String): CriteriaSearch {
        searchCriteria.extension = extension
        return this
    }

    override fun withName(name: String): CriteriaSearch {
        searchCriteria.name = name
        return this
    }

    override fun withContent(content: String): CriteriaSearch {
        searchCriteria.content = content
        return this
    }

    override fun traversal(): CriteriaSearch {
        traversalSearch = true
        return this
    }

    override fun execute(): Sequence<Path> {
        val files = getAllFiles()

        return files.filter { path ->
            extensionFilter(path) && nameFilter(path) && contentFilter(path)
        }
    }

    private fun contentFilter(path: Path): Boolean {
        val content = fileReader.from(path).getFileContent()
        return content.contains(searchCriteria.content) || searchCriteria.content.isEmpty()
    }

    private fun extensionFilter(path: Path): Boolean {
        return path.getExtension() == searchCriteria.extension || searchCriteria.extension.isEmpty()
    }

    private fun nameFilter(path: Path): Boolean {
// unfinished Idk if this should be like this
        val searchFileName = if (path.containsFileExtension()) path.getFileName() else path.justName()

        return searchFileName == searchCriteria.name || searchCriteria.name.isEmpty()
    }

    private fun getAllFiles(): Sequence<Path> {
        return if (traversalSearch) {
            paths.asSequence().flatMap {
                pathTraversal(it) }
        } else {
            paths.asSequence().flatMap { searchFilesDirectory(it) }
        }
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