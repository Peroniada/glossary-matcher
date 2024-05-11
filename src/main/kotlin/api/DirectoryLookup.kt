package com.sperek.file.api

import com.sperek.file.api.jvm.DirectoryPathTree
import com.sperek.file.api.jvm.JvmDirectory

interface DirectoryLookup {
    fun getDirectoryContent(directory: Path): DirectoryContent
    fun getDirectory(path: Path): Directory
    fun exists(path: Path): Boolean
}

interface Directory {
    val currentDir: Path
    val currentDirectoryName: String
    val currentDirectoryParent: Path
    val files: Set<Path>
    val directories: Set<JvmDirectory>
    fun getAllFilesTraversal(): Set<Path>
    fun toDirectoryPathTree(): DirectoryPathTree
}

data class DirectoryContent(val directories: Set<Path>, val files: Set<Path>)