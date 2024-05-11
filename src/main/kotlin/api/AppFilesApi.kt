package com.sperek.file.api

import com.sperek.file.api.jvm.JvmDirectoryLookup
import com.sperek.file.api.jvm.JvmSearchBuilder
import com.sperek.file.api.kotlinxio.KotlinXIOFileOperationBuilder
import com.sperek.file.api.kotlinxio.KotlinxIOFileSystem
import com.sperek.file.api.kotlinxio.filereader.KotlinXIOFileReader

class AppFilesApi(
    private val fileOperationBuilder: FileOperationBuilder,
    private val searchBuilder: SearchBuilder,
): FilesApi {

    override fun fileOperation(): FileOperationBuilder {
        return fileOperationBuilder
    }

    override fun search(): SearchBuilder {
        return searchBuilder
    }
}

object AppFilesApiFactory {
    fun create(): AppFilesApi {
        return AppFilesApi(
            KotlinXIOFileOperationBuilder(KotlinxIOFileSystem()),
            JvmSearchBuilder(JvmDirectoryLookup(), KotlinXIOFileReader(KotlinxIOFileSystem()))
        )
    }
}