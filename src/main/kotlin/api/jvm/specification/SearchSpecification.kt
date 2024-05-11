package com.sperek.file.api.jvm.specification

import com.sperek.file.api.FileReader
import com.sperek.file.api.Path
import com.sperek.file.api.search.Specification

class ContainsSubstringSpecification(
    private val substring: String,
    private val fileReader: FileReader
) : Specification<Path> {
    override fun isSatisfiedBy(t: Path): Boolean {
        val content = fileReader.from(t).getFileContent()
        return content.contains(substring)
    }
}

class FileExtensionSpecification(
    private val extension: String
) : Specification<Path> {
    override fun isSatisfiedBy(t: Path): Boolean {
        return t.getExtension() == extension
    }
}
