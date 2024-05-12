package org.example

import com.sperek.file.api.AppFilesApiFactory
import com.sperek.file.api.FilesApi
import com.sperek.file.api.Path

internal const val aap6plFilteredPagesFilePath = "src/main/resources/AAP6PL_filtered_pages.txt"
internal const val natoGlossaryFilteredFrench = "src/main/resources/NATO_Glossary_filtered_pages_french.txt"
fun main() {
    println("Hello World!")

    val filesApi = AppFilesApiFactory.create()
    val polishTxtPath = Path(aap6plFilteredPagesFilePath)

    val glossaryParser = GlossaryParser(filesApi)
    val polishToEnglishGlossaryString = glossaryParser.readFile(polishTxtPath)
    val cleanedUpGlossary = glossaryParser.cleanUpUnnecessaryContent(polishToEnglishGlossaryString)


}

/*
    example english to polish glossary definition:

            weapons free / tir libre
            In air defence, a weapon control order
            imposing a status whereby weapons
            systems may be fired at any target not
            positively recognized as friendly. 1/7/73
            ostrzał dowolny
            W obronie powietrznej komenda
            wprowadzająca stan, w którym systemy
            uzbrojenia mogą prowadzić ogień do
            każdego celu, który nie został
            rozpoznany jako własny. 18/10/12


            weapons free / tir libre                        -> english and french definition name

            In air defence, a weapon control order          -> english definition
            imposing a status whereby weapons
            systems may be fired at any target not
            positively recognized as friendly. 1/7/73

            ostrzał dowolny                                 -> polish definition name

            W obronie powietrznej komenda                   -> polish definition
            wprowadzająca stan, w którym systemy
            uzbrojenia mogą prowadzić ogień do
            każdego celu, który nie został
            rozpoznany jako własny. 18/10/12
 */

fun String.substringBetween(start: String, end: String): String {
    return this.substringAfter(start).substringBefore(end)
}

fun String.substringBetween(startIndex: Int, endIndex: Int): String {
    return this.substring(startIndex, endIndex)
}

data class EnglishPolishDefinition(
    val englishName: String,
    val frenchName: String,
    val englishDefinition: String,
    val polishName: String,
    val polishDefinition: String
) {
    companion object {
        val definitionEndRegex = Regex("\\d{1,2}/\\d{1,2}/\\d{1,2}(\\n)?")

        fun from(string: String): EnglishPolishDefinition {
            val englishName = string.substringBefore("/").trim()
            val frenchName = string.substringBetween("/", "\n").trim()
            val englishDefinitionEndString = definitionEndRegex.find(string)?.value ?: "\n"
            val englishDefinition = string.substringBetween(frenchName, englishDefinitionEndString).trim()
            val polishName = string.substringBetween(englishDefinitionEndString, "\n").trim()
            val polishDefinitionEndString = definitionEndRegex.find(string.substringAfter(polishName))?.value ?: "\n"
            val polishDefinition = string.substringBetween(polishName, polishDefinitionEndString).trim()
            return EnglishPolishDefinition(englishName, frenchName, englishDefinition, polishName, polishDefinition)
        }
    }
}

data class EnglishPolishDefinitions(
    val definitions: List<EnglishPolishDefinition>
) {
    companion object {
        fun from(string: String): EnglishPolishDefinitions {
            val definitionEndRegex = EnglishPolishDefinition.definitionEndRegex
            val definitionsEndMatches = definitionEndRegex.findAll(string)

            val definitionEndMatchesFiltered = definitionsEndMatches
                .filterIndexed { index, _ -> index % 2 == 1 }
            val definitions = definitionEndMatchesFiltered
                .mapIndexed { index, matchResult ->
                    val isStartOfString = index == 0
                    val matchResultValue = matchResult.value
                    val previousMatchIndex = definitionEndMatchesFiltered.indexOfFirst {  it.range == matchResult.range } - 1
                    val previousMatch = definitionEndMatchesFiltered.elementAtOrNull(previousMatchIndex)
                    getFullDefinitionSubstring(matchResult, string, previousMatch, isStartOfString)
                }
                .map { EnglishPolishDefinition.from(it) }
                .toList()

            return EnglishPolishDefinitions(definitions)
        }

        private fun getFullDefinitionSubstring(
            matchResult: MatchResult,
            glossary: String,
            previousMatch: MatchResult?,
            isStartOfString: Boolean
        ): String {
            return if (isStartOfString) {
                val endIndex = matchResult.range.last
                glossary.substringBetween(2, endIndex)
            } else {
                val startIndex = previousMatch?.range?.last ?: 0
                val endIndex = matchResult.next()?.range?.first ?: glossary.length
                glossary.substringBetween(startIndex, endIndex)
            }
        }
    }
}

class GlossaryParser(
    private val filesApi: FilesApi,
) {
    fun readFile(filePath: Path): String {
        return filesApi.fileOperation().read().from(filePath).getFileContent()
    }

    fun cleanUpUnnecessaryContent(glossary: String): String {
        return glossary
            .replace(FFAAP6_TO_REMOVE, "")
            .replace(NATO_PDP_JAWNE, "")
            .replace(NATO_PDP_JAWNE_WITH_PAGE_NUMBER_REGEX, "")
    }

    fun getDefinitionsGroupedByAlphabet(glossary: String): Map<Char, EnglishPolishDefinitions> {
        val result = dictionaryLetterIndicatorRegex.findAll(glossary)
        return result.groupBy(
            keySelector = { it.value[0] },
            valueTransform = {
                glossary.substringBetween(
                    it.range.first,
                    it.next()?.range?.first ?: glossary.length
                )
            }
        )
            .mapValues { it.value.first() }
            .mapValues { EnglishPolishDefinitions.from(it.value) }
    }


    /*
        rubbish
        AAP-6 (2017)
        NATO/PdP JAWNE
        NATO/PdP JAWNE 31
     */
    companion object {
        private val dictionaryLetterIndicatorRegex = Regex("(^[A-Z]$)", RegexOption.MULTILINE)

        private const val FFAAP6_TO_REMOVE = "AAP-6 (2017)"
        private const val NATO_PDP_JAWNE = "NATO/PdP JAWNE"
        private val NATO_PDP_JAWNE_WITH_PAGE_NUMBER_REGEX = Regex("$NATO_PDP_JAWNE \\d+")
    }

}
