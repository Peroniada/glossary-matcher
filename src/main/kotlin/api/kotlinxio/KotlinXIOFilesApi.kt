package com.sperek.file.api.kotlinxio

import com.sperek.file.api.*

class KotlinXIOFilesApi : FilesApi {

    override fun fileOperation(): FileOperationBuilder {
        return kotlinXIOFileOperationBuilder()
    }

    private fun kotlinXIOFileOperationBuilder(): KotlinXIOFileOperationBuilder {
        val kotlinxIOFileSystem = KotlinxIOFileSystem()
        return KotlinXIOFileOperationBuilder(kotlinxIOFileSystem)
    }

    override fun search(): SearchBuilder {
        return KotlinXIOSearchBuilder()
    }

}

