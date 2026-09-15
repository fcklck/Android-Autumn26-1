import model.Player
import model.Position
import model.Team
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ModelTest {
    @Test
    fun `positions ignore surrounding whitespace and letter case`() {
        Position.entries.forEach { position ->
            assertEquals(position, Position.fromCsvTokenOrNull("  ${position.name.lowercase()}\t"))
        }
        assertEquals(Position.MIDFIELD, Position.fromCsvTokenOrNull("MiDfIeLd"))
        assertNull(Position.fromCsvTokenOrNull("striker"))
        assertNull(Position.fromCsvTokenOrNull("  "))
    }

    @Test
    fun `every position has its Russian name`() {
        assertEquals(
            listOf("нападающий", "полузащитник", "защитник", "вратарь"),
            Position.entries.map { it.russianName },
        )
    }

    @Test
    fun `teams group their players and calculate squad statistics`() {
        val players = listOf(
            player("Антон", Team("Заря", "Пермь"), 100, 2, 1),
            player("Борис", Team("Заря", "Пермь"), 300, 4, 0),
            player("Виктор", Team("Заря", "Тюмень"), 50, 0, 2),
        )
        val teams = Team.fromPlayers(players)

        assertEquals(2, teams.size)
        assertEquals(players.take(2), teams[0].players)
        assertEquals(400L, teams[0].totalTransferCost)
        assertEquals(0.5, teams[0].averageRedCards)
        assertEquals(3.0, teams[0].averageYellowCards)
        assertEquals(listOf(players[2]), teams[1].players)
        assertEquals(50L, teams[1].totalTransferCost)
    }

    @Test
    fun `empty team has zero cost and card averages`() {
        val team = Team("Заря", "Пермь")

        assertEquals(0L, team.totalTransferCost)
        assertEquals(0.0, team.averageRedCards)
        assertEquals(0.0, team.averageYellowCards)
        assertEquals(emptyList(), Team.fromPlayers(emptyList()))
    }

    private fun player(name: String, team: Team, cost: Long, yellow: Int, red: Int) = Player(
        name = name,
        team = team,
        position = Position.DEFENDER,
        nationality = "Russia",
        agency = null,
        transferCost = cost,
        participations = 25,
        goals = 0,
        assists = 0,
        yellowCards = yellow,
        redCards = red,
    )
}
