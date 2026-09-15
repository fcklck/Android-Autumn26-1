import model.Position
import model.Team
import parser.CsvParser
import java.io.BufferedReader
import java.io.StringReader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CsvParserTest {
    private val header = "Name;Team;City;Position;Nationality;Agency;Transfer cost;Participations;Goals;Assists;Yellow cards;Red cards"

    @Test
    fun `parser trims text and maps all player fields`() {
        val player = assertNotNull(CsvParser.parseLine(row(mapOf(3 to " dEfEnDeR "))))

        assertEquals("Alex", player.name)
        assertEquals(Team("Dawn", "Perm"), player.team)
        assertEquals(Position.DEFENDER, player.position)
        assertEquals("Germany", player.nationality)
        assertEquals("Agency A", player.agency)
        assertEquals(60_000_000L, player.transferCost)
        assertEquals(25, player.participations)
        assertEquals(7, player.goals)
        assertEquals(3, player.assists)
        assertEquals(0, player.yellowCards)
        assertEquals(0, player.redCards)
    }

    @Test
    fun `empty agency is valid and becomes null`() {
        listOf("", "  ").forEach { agency ->
            val result = CsvParser.parse(sequenceOf(header, row(mapOf(5 to agency))))

            assertNull(result.players.single().agency)
            assertEquals(0, result.skippedCount)
        }
    }

    @Test
    fun `empty required text fields invalidate a row`() {
        listOf(0, 1, 2, 3, 4).forEach { column ->
            assertNull(CsvParser.parseLine(row(mapOf(column to "  "))), "Column $column")
        }
    }

    @Test
    fun `empty nonnumeric and overflowing numbers invalidate a row`() {
        (6..11).forEach { column ->
            listOf("", "  ", "oops", "999999999999999999999999").forEach { value ->
                assertNull(CsvParser.parseLine(row(mapOf(column to value))), "Column $column: $value")
            }
        }
    }

    @Test
    fun `wrong column count and unknown position invalidate a row`() {
        assertNull(CsvParser.parseLine("Alex;Dawn"))
        assertNull(CsvParser.parseLine(row() + ";extra"))
        assertNull(CsvParser.parseLine(row(mapOf(3 to "STRIKER"))))
        assertNull(CsvParser.parseLine(""))
    }

    @Test
    fun `parser skips header and counts each invalid data row`() {
        val result = CsvParser.parse(
            sequenceOf(header, row(), row(mapOf(6 to "unknown")), "", row(mapOf(5 to "")), "bad row"),
        )

        assertEquals(2, result.players.size)
        assertEquals(3, result.skippedCount)
        assertNull(result.players.last().agency)
    }

    @Test
    fun `empty file and header alone produce an empty result`() {
        listOf(emptySequence(), sequenceOf(header)).forEach { lines ->
            val result = CsvParser.parse(lines)
            assertTrue(result.players.isEmpty())
            assertEquals(0, result.skippedCount)
        }
    }

    @Test
    fun `reader overload parses rows and closes the reader`() {
        val reader = TrackingReader(listOf(header, row(), "bad row"))
        val result = CsvParser.parse(reader)

        assertEquals(1, result.players.size)
        assertEquals(1, result.skippedCount)
        assertTrue(reader.closed)
    }

    @Test
    fun `streaming search skips invalid rows and stops on the first suitable player`() {
        val reader = TrackingReader(
            listOf(
                header,
                "bad row",
                row(mapOf(6 to "50000000")),
                row(mapOf(7 to "24")),
                row(mapOf(10 to "1")),
                row(mapOf(11 to "1")),
                row(mapOf(0 to "First")),
                row(mapOf(0 to "Second")),
            ),
        )

        assertEquals("First", CsvParser.findFirstExpensiveDisciplinedPlayer(reader)?.name)
        assertEquals(7, reader.readCount)
        assertTrue(reader.closed)
    }

    @Test
    fun `streaming search returns null when no player meets every condition`() {
        val reader = TrackingReader(listOf(header, row(mapOf(11 to "1"))))

        assertNull(CsvParser.findFirstExpensiveDisciplinedPlayer(reader))
        assertTrue(reader.closed)
    }

    @Test
    fun `clean dataset parses all 250 players without skipped rows`() {
        val input = assertNotNull(javaClass.classLoader.getResourceAsStream("fakePlayers.csv"))
        val result = CsvParser.parse(input.reader(Charsets.UTF_8))

        assertEquals(250, result.players.size)
        assertEquals(0, result.skippedCount)
    }

    private fun row(replacements: Map<Int, String> = emptyMap()): String =
        listOf(" Alex ", " Dawn ", " Perm ", "FORWARD", " Germany ", " Agency A ", " 60000000 ", "25", "7", "3", "0", "0")
            .mapIndexed { index, value -> replacements[index] ?: value }
            .joinToString(";")

    private class TrackingReader(private val lines: List<String>) : BufferedReader(StringReader("")) {
        var readCount = 0
        var closed = false

        override fun readLine(): String? = lines.getOrNull(readCount++)

        override fun close() {
            closed = true
            super.close()
        }
    }
}
