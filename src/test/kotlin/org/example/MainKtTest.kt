package org.example

import com.sperek.file.api.AppFilesApiFactory
import com.sperek.file.api.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainKtTest {
    @Test
    fun testShouldReadFile() {
        //given
        val appFilesApi = AppFilesApiFactory.create()
        val glossaryParser = GlossaryParser(appFilesApi)

        //when
        val result = glossaryParser.readFile(Path(aap6plFilteredPagesFilePath))

        //then
        assertTrue(result.isNotEmpty())
    }

    @Test
    fun testShouldCreatePolishEnglishDefinition() {
        //given
        val definitionStr = """
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
        """.trimIndent()

        //when
        val result = EnglishPolishDefinition.from(definitionStr)

        //then
        val expectedEnglishDefinition = """
            In air defence, a weapon control order
            imposing a status whereby weapons
            systems may be fired at any target not
            positively recognized as friendly.
        """.trimIndent()


        val expectedPolishDefinition = """
                W obronie powietrznej komenda
                wprowadzająca stan, w którym systemy
                uzbrojenia mogą prowadzić ogień do
                każdego celu, który nie został
                rozpoznany jako własny.
            """.trimIndent()
        val expected = EnglishPolishDefinition(
            englishName = "weapons free",
            frenchName = "tir libre",
            englishDefinition = expectedEnglishDefinition,
            polishName = "ostrzał dowolny",
            polishDefinition = expectedPolishDefinition
        )

        assertEquals(expected, result)
    }

    @Test
    fun testShouldGetDefinitionsGroupedByAlphabet() {
        //given
        val appFilesApi = AppFilesApiFactory.create()
        val glossaryParser = GlossaryParser(appFilesApi)

        //when
        val uncleanedFile = glossaryParser.readFile(Path(aap6plFilteredPagesFilePath))
        println("uncleaned file length: ${uncleanedFile.length}")
        val cleanFile = glossaryParser.cleanUpUnnecessaryContent(uncleanedFile)
        val result = glossaryParser.getDefinitionsGroupedByAlphabet(cleanFile)
        assertEquals(26, result.size)
    }
}