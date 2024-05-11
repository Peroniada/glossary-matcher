package com.sperek.file.api.kotlinxio

import com.sperek.file.api.*
import com.sperek.file.api.kotlinxio.delete.KotlinXIODeleteOperation
import com.sperek.file.api.kotlinxio.filereader.KotlinXIOFileReader
import com.sperek.file.api.kotlinxio.filewriter.KotlinXIOFileWriter
import com.sperek.file.api.kotlinxio.move.KotlinXIOMoveOperation
import com.sperek.file.api.kotlinxio.rename.KotlinXIORenameOperation
import com.sperek.file.api.operation.DeleteOperation
import com.sperek.file.api.operation.MoveOperation
import com.sperek.file.api.operation.RenameOperation

class KotlinXIOFileOperationBuilder(private val kotlinxIOFileSystem: KotlinxIOFileSystem) : FileOperationBuilder {
    override fun create(): FileWriter {
        return KotlinXIOFileWriter(kotlinxIOFileSystem)
    }

    override fun read(): FileReader {
        return KotlinXIOFileReader(kotlinxIOFileSystem)
    }

    override fun delete(): DeleteOperation {
        return KotlinXIODeleteOperation(kotlinxIOFileSystem)
    }

    override fun rename(): RenameOperation {
        return KotlinXIORenameOperation(kotlinxIOFileSystem)
    }

    override fun move(): MoveOperation {
        return KotlinXIOMoveOperation(kotlinxIOFileSystem)
    }
}