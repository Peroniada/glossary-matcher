package com.sperek.file.api.jvm

import com.sperek.file.api.Directory
import com.sperek.file.api.DirectoryContent
import com.sperek.file.api.DirectoryLookup
import com.sperek.file.api.Path
import java.io.Closeable
import java.nio.file.Files
import java.nio.file.Paths

class JvmDirectoryLookup : DirectoryLookup {

    override fun getDirectory(path: Path): Directory {
        return JvmDirectory(this, path)
    }

    override fun exists(path: Path): Boolean {
        return path.toJvmPath().let { Files.exists(it) }
    }

    override fun getDirectoryContent(directory: Path): DirectoryContent {
        val path = directory.toJvmPath()

        return if (isExistingDirectory(path)) {
            withCloseable {
                val (directories, files) = Files.newDirectoryStream(path).use { paths ->
                    paths.partition { Files.isDirectory(it) }
                }
                val directoryPaths = directories.map { it.toDirectoryPath() }.toSet()
                val filePaths = files.map { it.toPath() }.toSet()

                DirectoryContent(directoryPaths, filePaths)
            }
        } else {
            DirectoryContent(emptySet(), emptySet())
        }
    }

    private fun isExistingDirectory(path: java.nio.file.Path) = Files.exists(path) && Files.isDirectory(path)

    private fun <R> withCloseable(block: () -> R): R {
        var closeable: Closeable? = null
        try {
            return block().also { closeable = it as? Closeable }
        } finally {
            closeable?.close()
        }
    }

    private fun Path.toJvmPath(): java.nio.file.Path {
        return Paths.get(this.asString())
    }

    private fun java.nio.file.Path.toPath(): Path {
        return Path(this.toString())
    }

    private fun java.nio.file.Path.toDirectoryPath(): Path {
        return Path(this.toString()).append("/")
    }

}

private const val PATH_IS_NOT_A_DIRECTORY = "Path is not a directory"

class JvmDirectory(
    val fileSystem: JvmDirectoryLookup,
    override val currentDir: Path
) : Directory {
    override val currentDirectoryName: String = currentDir.getFileName()
    override val currentDirectoryParent: Path = currentDir.getParent()
    override val files: Set<Path> by lazy { getDirectoryContent().files }
    override val directories: Set<JvmDirectory> by lazy { getChildDirectories() }

    init {
        check(currentDir.isDirectory()) { PATH_IS_NOT_A_DIRECTORY }
    }

    override fun getAllFilesTraversal(): Set<Path> {
        return files + directories.flatMap { it.getAllFilesTraversal() }
    }

    override fun toDirectoryPathTree(): DirectoryPathTree {
        return DirectoryPathTree(this)
    }

    fun buildNodes(): Set<Node<Path>> {
        return directories.map { buildNode(it) }.toSet()
    }
    private fun buildNode(directory: JvmDirectory): Node<Path> {
        val childrenDirectories: Set<Node<Path>> = directory.directories.map { buildNode(it) }.toSet()
        return Node(directory.currentDir, directory.files, childrenDirectories)
    }

    private fun getChildDirectories(): Set<JvmDirectory> {
        return getDirectoryContent().directories
            .map { JvmDirectory(fileSystem, it) }
            .toSet()
    }

    private fun getDirectoryContent() = fileSystem.getDirectoryContent(currentDir)

}

class DirectoryPathTree(private val root: JvmDirectory) {
    private val node: Node<Path> = Node(root.currentDir, root.files, getDirectoryPaths().toSet())

    private fun getDirectoryPaths(): Set<Node<Path>> {
        return root.directories.map { Node(it.currentDir, it.files, it.buildNodes().toSet()) }.toSet()
    }

    private val fileSystem = root.fileSystem
    fun retrieveAllFiles(): Set<Path> {
        return retrieveAllFiles(node)
    }
    private fun retrieveAllFiles(node: Node<Path>): Set<Path> {
        val allChildrenFiles = node.childrenDirectories.flatMap {
            JvmDirectory(fileSystem, it.value).getAllFilesTraversal()
        }.toSet()
        return node.childrenFiles + allChildrenFiles
    }

    fun printTree(): String {
        return printTree(node, "")
    }
    private fun printTree(node: Node<Path>, indent: String = ""): String {
        // print the current node
        val self = "$indent${if (node.isRoot) "" else "├── "}${node.value.getFileName()}"

        val newIndent = "$indent${if (node.isRoot) "" else "│   "}" // increase the indent for children

        // print directories under the current node with recursion
        val dirs = node.childrenDirectories.joinToString("\n") {
            printTree(it, newIndent)
        }

        // print files under the current node (they are leaves, so no recursion is necessary)
        val files = node.childrenFiles.joinToString("\n") {
            "$newIndent├── ${it.getFileName()}"
        }

        // combine the result
        return when {
            files.isNotBlank() && dirs.isNotBlank() -> "$self\n$dirs\n$files"
            files.isNotBlank() -> "$self\n$files"
            else -> "$self\n$dirs"
        }
    }
}

class Node<T>(val value: T, val childrenFiles: Set<T>, val childrenDirectories: Set<Node<T>>, val isRoot: Boolean = false) {
    init {
        check(childrenDirectories.none { it.value == value }) { "Node cannot be its own child" }
    }
}

