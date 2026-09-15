import model.Player
import model.Position
import model.Team
import visualization.countryShares
import visualization.topTeamValues
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class VisualizationTest {
    @Test
    fun `country percentages include every player and every country`() {
        val shares = countryShares(listOf(player("Brazil"), player("Germany"), player("Brazil")))

        assertEquals(listOf("Brazil", "Germany"), shares.map { it.first })
        assertEquals(200.0 / 3, shares.first().second, 0.0001)
        assertEquals(100.0, shares.sumOf { it.second }, 0.0001)
    }

    @Test
    fun `top ten cumulative share uses all teams as denominator`() {
        val teams = Team.fromPlayers((1..11).map { cost ->
            player("Brazil", Team("Team $cost", "City"), cost.toLong())
        })

        val values = topTeamValues(teams)

        assertEquals(10, values.size)
        assertEquals("Team 11", values.first().team.name)
        assertEquals(11.0 / 66 * 100, values.first().cumulativePercent, 0.0001)
        assertEquals(65.0 / 66 * 100, values.last().cumulativePercent, 0.0001)
        assertTrue(values.zipWithNext().all { (left, right) -> left.cumulativePercent <= right.cumulativePercent })
    }

    @Test
    fun `empty and zero cost data have no undefined shares`() {
        assertTrue(countryShares(emptyList()).isEmpty())
        assertTrue(topTeamValues(emptyList()).isEmpty())
        assertEquals(0.0, topTeamValues(Team.fromPlayers(listOf(player("Brazil", cost = 0)))).single().cumulativePercent)
    }

    private fun player(country: String, team: Team = Team("Team", "City"), cost: Long = 10) = Player(
        name = "Player",
        team = team,
        position = Position.FORWARD,
        nationality = country,
        agency = null,
        transferCost = cost,
        participations = 1,
        goals = 0,
        assists = 0,
        yellowCards = 0,
        redCards = 0
    )
}
