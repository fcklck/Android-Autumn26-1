import model.Player
import model.Position
import model.Team
import model.TeamSquad
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
    fun `squads group players while teams keep only name and city`() {
        val players = listOf(
            player("Антон", Team("Заря", "Пермь"), 100, 2, 1),
            player("Борис", Team("Заря", "Пермь"), 300, 4, 0),
            player("Виктор", Team("Заря", "Тюмень"), 50, 0, 2),
        )
        val teams = TeamSquad.fromPlayers(players)

        assertEquals(2, teams.size)
        assertEquals(Team("Заря", "Пермь"), teams[0].team)
        assertEquals(players.take(2), teams[0].players)
        assertEquals(400L, teams[0].totalTransferCost)
        assertEquals(0.5, teams[0].averageRedCards)
        assertEquals(listOf(players[2]), teams[1].players)
        assertEquals(50L, teams[1].totalTransferCost)
    }

    @Test
    fun `empty player list has no squads`() {
        val team = Team("Заря", "Пермь")

        assertEquals(Team("Заря", "Пермь"), team)
        assertEquals(emptyList(), TeamSquad.fromPlayers(emptyList()))
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
