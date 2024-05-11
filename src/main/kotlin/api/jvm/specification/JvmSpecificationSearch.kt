package com.sperek.file.api.jvm.specification

import com.sperek.file.api.Path
import com.sperek.file.api.search.Specification
import com.sperek.file.api.search.SpecificationSearch
import com.sperek.file.api.jvm.JvmDirectoryLookup

class JvmSpecificationSearch(private val jvmDirectoryLookup: JvmDirectoryLookup) : SpecificationSearch {
    private var traversal = false
    private lateinit var searchRoot: Path
    private lateinit var specification: Specification<Path>
    override fun withRoot(path: Path): SpecificationSearch {
        check(path.isDirectory()) { "Root path must be a directory" }
        this.searchRoot = path
        return this
    }

    override fun traversal(): SpecificationSearch {
        this.traversal = true
        return this
    }

    override fun withSpecification(specification: Specification<Path>): SpecificationSearch {
        this.specification = specification
        return this
    }


    override fun execute(): Sequence<Path> {
        val files = if (traversal) {
            jvmDirectoryLookup.getDirectory(searchRoot).getAllFilesTraversal()
        } else {
            jvmDirectoryLookup.getDirectoryContent(searchRoot).files
        }.asSequence()

        return filterBySpecification(files)
    }

    private fun filterBySpecification(files: Sequence<Path>): Sequence<Path> {
        return files.filter { specification.isSatisfiedBy(it) }
    }
}