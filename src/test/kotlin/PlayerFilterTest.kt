import filter.PlayerFilter
import model.Player
import model.Position
import model.Team
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PlayerFilterTest {
    private val player = Player(
        name = "Alex",
        team = Team("Dawn", "Perm"),
        position = Position.DEFENDER,
        nationality = "Russia",
        agency = null,
        transferCost = 100,
        participations = 1,
        goals = 2,
        assists = 3,
        yellowCards = 0,
        redCards = 0,
    )

    @Test
    fun `filters compose recursively`() {
        val defender = PlayerFilter.ByPosition(Position.DEFENDER)
        val cheap = PlayerFilter.CheaperThan(101)

        assertTrue(defender.matches(player))
        assertTrue(PlayerFilter.And(defender, cheap).matches(player))
        assertTrue(PlayerFilter.Or(PlayerFilter.ByPosition(Position.FORWARD), defender).matches(player))
        assertTrue(PlayerFilter.Not(PlayerFilter.CheaperThan(100)).matches(player))
        assertFalse(PlayerFilter.And(defender, PlayerFilter.CheaperThan(100)).matches(player))
    }
}
