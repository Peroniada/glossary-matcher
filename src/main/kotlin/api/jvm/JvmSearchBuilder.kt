package com.sperek.file.api.jvm

import com.sperek.file.api.*
import com.sperek.file.api.jvm.criteria.JvmCriteriaSearch
import com.sperek.file.api.jvm.simple.JvmSimpleSearch
import com.sperek.file.api.jvm.specification.JvmSpecificationSearch
import com.sperek.file.api.search.CriteriaSearch
import com.sperek.file.api.search.SimpleSearch
import com.sperek.file.api.search.SpecificationSearch

class JvmSearchBuilder(private val jvmDirectoryLookup: JvmDirectoryLookup, private val fileReader: FileReader) : SearchBuilder {
    override fun simpleSearch(): SimpleSearch {
        return JvmSimpleSearch(jvmDirectoryLookup)
    }

    override fun criteriaSearch(): CriteriaSearch {
        return JvmCriteriaSearch(jvmDirectoryLookup, fileReader)
    }

    override fun  specificationSearch(): SpecificationSearch {
        return JvmSpecificationSearch(jvmDirectoryLookup)
    }
}




