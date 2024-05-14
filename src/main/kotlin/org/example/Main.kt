package org.example

import com.sperek.file.api.AppFilesApiFactory
import com.sperek.file.api.FilesApi
import com.sperek.file.api.Path

internal const val aap6plFilteredPagesFilePath = "src/main/resources/cleanedGlossaryENG.txt"
internal const val glossaryFilteredFrench = "src/main/resources/cleanedGlossaryFR.txt"
fun main() {
    println("Hello World!")
    val start = System.currentTimeMillis()
    val filesApi = AppFilesApiFactory.create()
    val polishTxtPath = Path(aap6plFilteredPagesFilePath)
    val frenchTxtPath = Path(glossaryFilteredFrench)

    val glossaryParser = GlossaryParser(filesApi)
    val parsePolishGlossary = glossaryParser.parsePolishGlossary(glossaryParser, polishTxtPath)
    val parseFrenchGlossary = glossaryParser.parseFrenchGlossary(glossaryParser, frenchTxtPath)
    val serializableGlossary = SerializableGlossary(parsePolishGlossary, parseFrenchGlossary)
    val allLettersEntriesEnglish = AllLettersEntries(
        serializableGlossary.getEnglishKeyOrderedGlossaryDefinitions().mapValues { SerializableEntriesStartingWithLetter(it.key, it.value) }
    )
    val allLettersEntriesFrench = AllLettersEntries(
        serializableGlossary.getFrenchKeyOrderedGlossaryDefinitions().mapValues { SerializableEntriesStartingWithLetter(it.key, it.value) }
    )

    val toEnglishKeyMarkdown = allLettersEntriesEnglish.toEnglishKeyMarkdown()
    val toFrenchKeyMarkdown = allLettersEntriesFrench.toFrenchKeyMarkdown()

    val englishKeyMarkdownPath = Path("src/main/resources/englishKeyMarkdown.md")
    val frenchKeyMarkdownPath = Path("src/main/resources/frenchKeyMarkdown.md")

    filesApi.fileOperation().create().into(englishKeyMarkdownPath).withContent(toEnglishKeyMarkdown).save()
    filesApi.fileOperation().create().into(frenchKeyMarkdownPath).withContent(toFrenchKeyMarkdown).save()
    val end = System.currentTimeMillis()
    val timeInSeconds = (end - start) / 1000
    println("Time in seconds: $timeInSeconds")
}

// it will print as markdown
/*
example english key printable:
---
#### EN: Urgent mining
In naval mine warfare, the laying of mines with correct spacing but not in the
ordered or planned positions. The mines may be laid either inside or outside the
allowed area in such positions that they will hamper the movements of the enemy
more than those of our own forces.
#### PL: Pośpieszne minowanie
W morskich działaniach minowych, stawianie min w prawidłowych odległościach,
ale nie w nakazanych lub planowanych miejscach. Miny mogą być stawiane albo
wewnątrz, albo na zewnątrz rejonu dozwolonego w takich miejscach, aby opóźniać ruchy
sił przeciwnika bardziej niż sił własnych.
#### FR: Mouillage des mines d'urgence
Mouillage de mines avec un espacement correct mais non à la position prévue.
Les mines sont mouillées à l'intérieur ou à l'extérieur de la zone fixée,
de manière à gêner davantage les mouvements ennemis que les mouvements amis.

---
 */

data class AllLettersEntries(
    val serializableEntriesStartingWithLetterByLetter: Map<Char,SerializableEntriesStartingWithLetter>
) {
    fun toEnglishKeyMarkdown(): String {
        return """
            |# English - Polish - French
            |---
            |${serializableEntriesStartingWithLetterByLetter.mapValues { it.value.toEnglishKeyMarkdown() }.values.joinToString { it }}
        """.trimMargin()
    }
    fun toFrenchKeyMarkdown(): String {
        return """
            |# French - Polish - English
            |---
            |${serializableEntriesStartingWithLetterByLetter.mapValues { it.value.toFrenchKeyMarkdown() }.values.joinToString { it }}
        """.trimMargin()
    }


}

data class SerializableEntriesStartingWithLetter(
    val letter: Char,
    val entries: List<SerializableGlossaryEntry>
) {
    fun toEnglishKeyMarkdown(): String {
        return """
            |## ${letter.toString()}
            |---
            |${entries.joinToString("\n") { it.toEnglishKeyMarkdown() }}
        """.trimMargin()
    }

    fun toFrenchKeyMarkdown(): String {
        return """
            |## ${letter.toString()}
            |---
            |${entries.joinToString("\n") { it.toFrenchKeyMarkdown() }}
        """.trimMargin()
    }
}

data class SerializableGlossaryEntry(
    val polishName: String,
    val englishName: String,
    val frenchName: String,
    val polishDefinition: String,
    val englishDefinition: String,
    val frenchDefinition: String
) {
    fun toEnglishKeyMarkdown(): String {
        return """
            #### EN: $englishName
            $englishDefinition
            #### PL: $polishName
            $polishDefinition
            #### FR: $frenchName
            $frenchDefinition
            
            ---
        """.trimIndent()
    }

    fun toFrenchKeyMarkdown(): String {
        return """
            #### FR: $frenchName
            $frenchDefinition
            #### PL: $polishName
            $polishDefinition
            #### EN: $englishName
            $englishDefinition
            
            ---
        """.trimIndent()
    }

    fun toPolishKeyMarkdown(): String {
        return """
            #### PL: $polishName
            $polishDefinition
            #### EN: $englishName
            $englishDefinition
            #### FR: $frenchName
            $frenchDefinition
            
            ---
        """.trimIndent()
    }
}

class SerializableGlossary(
    private val polishGlossary: Map<Char, EnglishPolishDefinitions>,
    private val frenchGlossary: Map<Char, FrenchDefinitions>
) {
    fun getEnglishKeyOrderedGlossaryDefinitions(): Map<Char, List<SerializableGlossaryEntry>> {
        val allFrenchGlossaryEntriesByEnglishName = getAllFrenchGlossaryEntriesByEnglishName()
        return polishGlossary.mapValues { (_, definitions) ->
            definitions.definitions.map { polishDefinition ->
                val frenchDefinition = allFrenchGlossaryEntriesByEnglishName[polishDefinition.englishName]
                SerializableGlossaryEntry(
                    polishName = polishDefinition.polishName,
                    englishName = polishDefinition.englishName,
                    frenchName = frenchDefinition?.frenchName ?: "",
                    polishDefinition = polishDefinition.polishDefinition,
                    englishDefinition = polishDefinition.englishDefinition,
                    frenchDefinition = frenchDefinition?.frenchDefinition ?: ""
                )
            } .filter { it.frenchDefinition.isNotEmpty() }
        }
    }

    fun getFrenchKeyOrderedGlossaryDefinitions(): Map<Char, List<SerializableGlossaryEntry>> {
        val allPolishGlossaryEntriesByEnglishName = getAllPolishGlossaryEntriesByEnglishName()
        return frenchGlossary.mapValues { (_, definitions) ->
            definitions.definitions.map { frenchDefinition ->
                val polishDefinition = allPolishGlossaryEntriesByEnglishName[frenchDefinition.englishName]
                SerializableGlossaryEntry(
                    polishName = polishDefinition?.polishName ?: "",
                    englishName = frenchDefinition.englishName,
                    frenchName = frenchDefinition.frenchName,
                    polishDefinition = polishDefinition?.polishDefinition ?: "",
                    englishDefinition = polishDefinition?.englishDefinition ?: "",
                    frenchDefinition = frenchDefinition.frenchDefinition
                )
            } .filter { it.polishDefinition.isNotEmpty() }
        }
    }

    private fun getAllPolishGlossaryEntriesByEnglishName(): Map<String, EnglishPolishDefinition> {
        return polishGlossary.flatMap { it.value.definitions }
            .associateBy { it.englishName }
    }
    private fun getAllFrenchGlossaryEntriesByEnglishName(): Map<String, FrenchDefinition> {
        return frenchGlossary.flatMap { it.value.definitions }
            .associateBy { it.englishName }
    }
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
    return this.substringAfter(start).trim().substringBefore(end)
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
        val definitionEndRegex =
            Regex("(\\.\\s*\\d{1,2}\\/\\d{1,2}\\/\\d{1,2})|(\\.\\s\\[.*\\s*.*\\]\\s\\d{1,2}\\/\\d{1,2}\\/\\d{1,2})|(\\.\\s.*,*\\s*\\d{1,2}\\/\\d{1,2}\\/\\d{1,2})|(\\.\\s\\[.*\\s*.*\\]\\s)\\n?")
        val englishDefinitionStartRegex = Regex("^[A-Z].*$", RegexOption.MULTILINE)

        fun from(string: String): EnglishPolishDefinition {
            val englishName = string.substringBefore("/").trim().replace(Regex("^\\W+"), "").replace("\n", " ")
            val englishDefinitionStartStr =
                englishDefinitionStartRegex.find(string.substringAfter(englishName))?.value ?: "\n"
            val frenchName = string.substringBetween("/", englishDefinitionStartStr).trim().replace("\n", " ")
            val englishDefinitionEndString = definitionEndRegex.find(string)?.value ?: "\n"
            val englishDefinition =
                string.substringBetween(frenchName, englishDefinitionEndString).trim().replace("\n", " ")
            val polishName = string.substringBetween(englishDefinitionEndString, "\n").trim()
            val polishDefinitionEndString = definitionEndRegex.find(string.substringAfter(polishName))?.value ?: "\n"
            val polishDefinition =
                string.substringBetween(polishName, polishDefinitionEndString).trim().replace("\n", " ")
            return EnglishPolishDefinition(englishName, frenchName, englishDefinition, polishName, polishDefinition)
        }
    }
}

data class EnglishPolishDefinitions(
    val definitions: List<EnglishPolishDefinition>
) {
    fun getEntryByEnglishNameMap(): Map<String, EnglishPolishDefinition> {
        return definitions.associateBy { it.englishName }
    }

    companion object {
        fun from(string: String): EnglishPolishDefinitions {
            val definitionEndRegex = EnglishPolishDefinition.definitionEndRegex
            val definitionsEndMatches = definitionEndRegex.findAll(string)

            val definitionEndMatchesFiltered = definitionsEndMatches
                .filterIndexed { index, _ -> index % 2 == 1 }
            val definitionsStr = definitionEndMatchesFiltered
                .mapIndexed { index, matchResult ->
                    val isStartOfString = index == 0
                    val previousMatchIndex =
                        definitionEndMatchesFiltered.indexOfFirst { it.range == matchResult.range } - 1
                    val previousMatch = definitionEndMatchesFiltered.elementAtOrNull(previousMatchIndex)
                    getFullDefinitionSubstring(matchResult, string, previousMatch, isStartOfString).trim()
                }
            val definitions = definitionsStr
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
                val startIndex = previousMatch?.range?.last ?: 2
                val endIndex = matchResult.range.last
//                val endIndex = matchResult.next()?.range?.first ?: glossary.length
                glossary.substringBetween(startIndex + 1, endIndex).trim()
            }
        }
    }
}

data class FrenchDefinitions(
    val definitions: List<FrenchDefinition>
) {
    fun getEntryByEnglishNameMap(): Map<String, FrenchDefinition> {
        return definitions.associateBy { it.englishName }
    }

    companion object {
        fun from(string: String): FrenchDefinitions {
            val definitionEndRegex = FrenchDefinition.definitionEndRegex
            val definitionsEndMatches = definitionEndRegex.findAll(string)

            val definitionsStr = definitionsEndMatches
                .mapIndexed { index, matchResult ->
                    val isStartOfString = index == 0
                    val previousMatchIndex =
                        definitionsEndMatches.indexOfFirst { it.range == matchResult.range } - 1
                    val previousMatch = definitionsEndMatches.elementAtOrNull(previousMatchIndex)
                    getFullDefinitionSubstring(matchResult, string, previousMatch, isStartOfString).trim()
                }
            val definitions = definitionsStr
                .map { FrenchDefinition.from(it) }
                .toList()

            return FrenchDefinitions(definitions)
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
                val startIndex = previousMatch?.range?.last ?: 2
                val endIndex = matchResult.range.last + 1
//                val endIndex = matchResult.next()?.range?.first ?: glossary.length
                glossary.substringBetween(startIndex + 1, endIndex).trim()
            }
        }
    }
}

data class FrenchDefinition(
    val frenchName: String,
    val englishName: String,
    val frenchDefinition: String
) {
    companion object {
        val definitionEndRegex = Regex("(\\d{4}\\.\\d{2}\\.\\d{2})", RegexOption.MULTILINE)
        val frenchDefinitionStartRegex = Regex("^[A-Z].*$", RegexOption.MULTILINE)
        fun from(string: String): FrenchDefinition {
            val frenchName = string.substringBefore("/").trim().replace(Regex("^\\W+"), "").replace("\n", " ")
            val frenchDefinitionStartStr =
                frenchDefinitionStartRegex.find(string.substringAfter(frenchName))?.value ?: "\n"
            val englishName = string.substringBetween("/", frenchDefinitionStartStr).trim().replace(Regex("^\\W+"), "").replace("\n", " ")
            val frenchDefinitionEndString = definitionEndRegex.find(string)?.value ?: "\n"
            val frenchDefinition = string.substringBetween(englishName, frenchDefinitionEndString).trim().replace("\n", " ")
            return FrenchDefinition(frenchName, englishName, frenchDefinition)
        }
    }
}

class GlossaryParser(
    private val filesApi: FilesApi,
) {

    fun parsePolishGlossary(glossaryParser: GlossaryParser, polishTxtPath: Path): Map<Char, EnglishPolishDefinitions> {
        val polishToEnglishGlossaryString = glossaryParser.readFile(polishTxtPath)
        val cleanedUpGlossary = glossaryParser.cleanUpUnnecessaryContentPolishEnglish(polishToEnglishGlossaryString)
        return glossaryParser.getPolishEnglishDefinitionsByLetter(cleanedUpGlossary)
    }

    fun parseFrenchGlossary(glossaryParser: GlossaryParser, frenchTxtPath: Path): Map<Char, FrenchDefinitions> {
        val frenchGlossaryString = glossaryParser.readFile(frenchTxtPath)
//        val cleanedUpFrenchGlossary = glossaryParser.cleanUpUnnecessaryContentFrench(frenchGlossaryString)
        return glossaryParser.getFrenchDefinitionsByLetter(frenchGlossaryString)
    }

    fun readFile(filePath: Path): String {
        return filesApi.fileOperation().read().from(filePath).getFileContent()
    }

    fun cleanUpUnnecessaryContentPolishEnglish(glossary: String): String {
        val glossaryWithoutPreferredTerms = cleanUpPreferredTermEntries(glossary)
        val glossaryWithoutLineNumberStrings =
            NATO_PDP_JAWNE_WITH_PAGE_NUMBER_REGEX.replace(glossaryWithoutPreferredTerms, "")
        val repeatingDatesCleaned =
            repeatingDateLikeStringsRegex.replace(glossaryWithoutLineNumberStrings) { matchResult -> matchResult.groupValues[1] }
        return repeatingDatesCleaned
            .replace(FFAAP6_TO_REMOVE, "")
            .replace(NATO_PDP_JAWNE, "")
    }

    fun cleanUpUnnecessaryContentFrench(glossary: String): String {
        val glossaryWithoutPreferredTerms = cleanUpPreferredTermFrenchEntries(glossary)
        val frenchPageNumbersCleaned =
            Regex("-\\d{1,4}-", RegexOption.MULTILINE).replace(glossaryWithoutPreferredTerms, "")
        return frenchPageNumbersCleaned
            .replace(FFAAP6_TO_REMOVE_FR, "")
    }

    private fun cleanUpPreferredTermFrenchEntries(glossary: String): String {
        val glossaryWithoutPreferredTerms = preferredTermFRRegex.replace(glossary, "")
        return glossaryWithoutPreferredTerms
    }

    /*
      example glossary entry:
              środków bojowych i materiałowych.
              26/2/01
              airmiss
              Preferred term: near miss.
              niebezpieczne zbliżenie
              >Termin zalecany: near miss. 22/9/99
              airmobile forces / force aéromobile
              The ground combat, supporting and air

              expected output:
              środków bojowych i materiałowych.
              26/2/01
              airmobile forces / force aéromobile
              The ground combat, supporting and air

              things to cut:
              airmiss
              Preferred term: near miss.
              niebezpieczne zbliżenie
              >Termin zalecany: near miss. 22/9/99
     */
    private fun cleanUpPreferredTermEntries(glossary: String): String {
        val glossaryWithoutPreferredTermsEng = preferredTermEngRegex.replace(glossary, "")
        val glossaryWithoutPreferredTermsPl = preferredTermPlRegex.replace(glossaryWithoutPreferredTermsEng, "")
        return glossaryWithoutPreferredTermsPl
    }

    fun getPolishEnglishDefinitionsByLetter(glossary: String): Map<Char, EnglishPolishDefinitions> {
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

    fun getFrenchDefinitionsByLetter(glossary: String): Map<Char, FrenchDefinitions> {
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
            .mapValues { FrenchDefinitions.from(it.value) }
    }


    /*
        rubbish
        AAP-6 (2017)
        NATO/PdP JAWNE
        NATO/PdP JAWNE 31
     */
    companion object {
        private val dictionaryLetterIndicatorRegex = Regex("(^[A-Z]$)", RegexOption.MULTILINE)
        private const val PREFERRED_TERM_ENG = "Preferred term:"
        private val preferredTermEngRegex = Regex(".*\n$PREFERRED_TERM_ENG.*(\\n?.*){1,3}\\.", RegexOption.MULTILINE)
        private const val PREFERRED_TERM_PL = ">Termin zalecany:"
        val preferredTermPlRegex =
            Regex(".*\n$PREFERRED_TERM_PL.*\\n\\w*\\.*\\s*(\\d{1,2}/\\d{1,2}/\\d{1,2})*\\n*", RegexOption.MULTILINE)
        val preferredTermFRRegex = Regex("(.*\\sTerme privilégié :.*\\s.*\\.)", RegexOption.MULTILINE)
        const val FFAAP6_TO_REMOVE = "AAP-6 (2017)"
        const val FFAAP6_TO_REMOVE_FR = "AAP-06(2017)"
        const val NATO_PDP_JAWNE = "NATO/PdP JAWNE"
        val NATO_PDP_JAWNE_WITH_PAGE_NUMBER_REGEX = Regex("$NATO_PDP_JAWNE \\d{1,4}", RegexOption.MULTILINE)
        val repeatingDateLikeStringsRegex = Regex("(\\d{1,2}/\\d{1,2}/\\d{1,2})\\s*(\\d{1,2}/\\d{1,2}/\\d{1,2})")

    }

}
