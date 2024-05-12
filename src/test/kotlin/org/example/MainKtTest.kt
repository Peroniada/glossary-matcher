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

    @Test
    fun testShouldCutPreferredTermsEntry() {
        //given
        val exampleEntry = """
            air logistic support operation /
            opération aérienne de soutien
            logistique
            An air operation, excluding an airborne
            operation, conducted within a theatre
            of operations to distribute and/or
            recover personnel, equipment and
            supplies. 4/10/00
            wsparcie logistyczne z powietrza
            Operacja powietrzna, z wyjątkiem
            operacji desantowych, prowadzona na
            teatrze działań, w celu dostarczenia
            i uzupełnienia stanu osobowego sprzętu,
            środków bojowych i materiałowych.
            26/2/01
            airmiss
            Preferred term: near miss.
            niebezpieczne zbliżenie
            >Termin zalecany: near miss. 22/9/99
            airmobile forces / force aéromobile
            The ground combat, supporting and air
            vehicle units required to conduct an
            airmobile operation. 1/3/79
        """.trimIndent()

        val glossaryParser = GlossaryParser(AppFilesApiFactory.create())
        //when
        val cleanedEntry = glossaryParser.cleanUpUnnecessaryContent(exampleEntry)

        //then
        val expected = """
            air logistic support operation /
            opération aérienne de soutien
            logistique
            An air operation, excluding an airborne
            operation, conducted within a theatre
            of operations to distribute and/or
            recover personnel, equipment and
            supplies. 4/10/00
            wsparcie logistyczne z powietrza
            Operacja powietrzna, z wyjątkiem
            operacji desantowych, prowadzona na
            teatrze działań, w celu dostarczenia
            i uzupełnienia stanu osobowego sprzętu,
            środków bojowych i materiałowych.
            26/2/01
            airmobile forces / force aéromobile
            The ground combat, supporting and air
            vehicle units required to conduct an
            airmobile operation. 1/3/79
        """.trimIndent()

        assertEquals(expected, cleanedEntry)
    }
    
    @Test
    fun testShouldClearGlossary() {
        //given
        val appFilesApi = AppFilesApiFactory.create()
        val glossaryParser = GlossaryParser(appFilesApi)

        //when
        val result = glossaryParser.readFile(Path(aap6plFilteredPagesFilePath))
        val cleanedResult = glossaryParser.cleanUpUnnecessaryContent(result)

        appFilesApi.fileOperation().create().into(Path("src/test/resources/cleanedGlossaryENG.txt")).withContent(cleanedResult).save()
        
        //then
        val doesNotContainFFAAP6 =
            assertTrue(cleanedResult.lines().none { it.contains(GlossaryParser.FFAAP6_TO_REMOVE) })
            assertTrue(cleanedResult.lines().none { it.contains(GlossaryParser.NATO_PDP_JAWNE) })
            assertTrue(GlossaryParser.NATO_PDP_JAWNE_WITH_PAGE_NUMBER_REGEX.findAll(cleanedResult).none())
    }
}