import model.Player
import model.Position
import model.Team
import resolver.Resolver
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ResolverTest {
    private val dawn = Team("Dawn", "Perm")
    private val north = Team("North", "Kazan")

    private val players = listOf(
        player("Zed", dawn, Position.DEFENDER, "Germany", "Bravo", 100, goals = 4, assists = 1, red = 2),
        player("Adam", dawn, Position.DEFENDER, "Germany", "Alpha", 300, goals = 4, assists = 2, red = 0),
        player("Bella", north, Position.FORWARD, "Brazil", "Alpha", 200, goals = 1, assists = 8, red = 1),
        player("Cara", north, Position.GOALKEEPER, "Brazil", null, 400, goals = 0, assists = 0, red = 3),
    )

    private val resolver = Resolver(players)

    @Test
    fun `queries one through four use their specified ranking and averages`() {
        assertEquals(1, resolver.getCountWithoutAgency())
        assertEquals("Adam" to 4, resolver.getBestScorerDefender())
        assertEquals("защитник", resolver.getTheExpensiveGermanPlayerPosition())
        assertEquals("North", resolver.getTheRudestTeam().name)
        assertEquals("Kazan", resolver.getTheRudestTeam().city)
    }

    @Test
    fun `average costs are descending and useful players use assists twice`() {
        assertEquals(
            listOf(Position.GOALKEEPER, Position.DEFENDER, Position.FORWARD),
            resolver.getAverageTransferCostByPosition().keys.toList(),
        )
        assertEquals(listOf("Bella", "Adam", "Zed"), resolver.getMostValuablePlayers().map { it.name })
    }

    @Test
    fun `popular agencies ignore null and break ties alphabetically`() {
        assertEquals(mapOf("Brazil" to "Alpha", "Germany" to "Alpha"), resolver.getMostPopularAgencyByCountry())
    }

    @Test
    fun `median shares include players exactly on the median`() {
        val medianPlayers = players + player("Equal", dawn, Position.MIDFIELD, "Russia", "Alpha", 250, goals = 5)
        assertEquals(10.0 / 14.0 to 4.0 / 14.0, Resolver(medianPlayers).getGoalsShareByMedianCost())

        val noGoals = players.map { it.copy(goals = 0) }
        assertEquals(0.0 to 0.0, Resolver(noGoals).getGoalsShareByMedianCost())
    }

    @Test
    fun `empty singular queries fail clearly while collection queries are empty`() {
        val empty = Resolver(emptyList())
        assertFailsWith<NoSuchElementException> { empty.getBestScorerDefender() }
        assertFailsWith<NoSuchElementException> { empty.getTheExpensiveGermanPlayerPosition() }
        assertFailsWith<NoSuchElementException> { empty.getTheRudestTeam() }
        assertEquals(emptyMap(), empty.getAverageTransferCostByPosition())
        assertEquals(emptyList(), empty.getMostValuablePlayers())
        assertEquals(emptyMap(), empty.getMostPopularAgencyByCountry())
        assertEquals(0.0 to 0.0, empty.getGoalsShareByMedianCost())
    }

    private fun player(
        name: String,
        team: Team,
        position: Position,
        nationality: String,
        agency: String?,
        cost: Long,
        goals: Int,
        assists: Int = 0,
        red: Int = 0,
    ) = Player(
        name = name,
        team = team,
        position = position,
        nationality = nationality,
        agency = agency,
        transferCost = cost,
        participations = 25,
        goals = goals,
        assists = assists,
        yellowCards = 0,
        redCards = red,
    )
}
