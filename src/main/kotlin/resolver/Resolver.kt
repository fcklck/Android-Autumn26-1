package resolver

import filter.PlayerFilter
import model.Player
import model.Position
import model.Team

class Resolver(private val players: List<Player>) : IResolver {
    val teams: List<Team> by lazy { Team.fromPlayers(players) }

    private val sortedCosts by lazy { players.map { it.transferCost }.sorted() }

    val medianTransferCost: Double by lazy {
        if (sortedCosts.isEmpty()) 0.0
        else sortedCosts[(sortedCosts.size - 1) / 2] / 2.0 + sortedCosts[sortedCosts.size / 2] / 2.0
    }

    override fun getCountWithoutAgency(): Int = players.count { it.agency == null }

    override fun getBestScorerDefender(): Pair<String, Int> = players
        .filter(PlayerFilter.ByPosition(Position.DEFENDER)::matches)
        .sortedWith(compareByDescending<Player> { it.goals }.thenBy { it.name })
        .firstOrNull()
        ?.let { it.name to it.goals }
        ?: throw NoSuchElementException("В выборке нет защитников")

    override fun getTheExpensiveGermanPlayerPosition(): String = players
        .filter { it.nationality == "Germany" }
        .sortedWith(compareByDescending<Player> { it.transferCost }.thenBy { it.name })
        .firstOrNull()
        ?.position?.russianName
        ?: throw NoSuchElementException("В выборке нет немецких игроков")

    override fun getTheRudestTeam(): Team = teams
        .sortedWith(compareByDescending<Team> { it.averageRedCards }.thenBy { it.name }.thenBy { it.city })
        .firstOrNull()
        ?: throw NoSuchElementException("В выборке нет команд")

    override fun getAverageTransferCostByPosition(): Map<Position, Double> = players
        .groupBy { it.position }
        .mapValues { (_, players) -> players.map { it.transferCost }.average() }
        .entries
        .sortedWith(compareByDescending<Map.Entry<Position, Double>> { it.value }.thenBy { it.key.name })
        .associate { it.key to it.value }

    override fun getMostValuablePlayers(): List<Player> = players
        .sortedWith(compareByDescending<Player> { it.goals.toLong() + 2L * it.assists }.thenBy { it.name })
        .take(3)

    /** null означает отсутствие агентства; страны без агентств не попадают в результат. */
    override fun getMostPopularAgencyByCountry(): Map<String, String> = players
        .mapNotNull { player -> player.agency?.let { player.nationality to it } }
        .groupBy({ it.first }, { it.second })
        .toSortedMap()
        .mapValues { (_, agencies) ->
            agencies.groupingBy { it }.eachCount().entries
                .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
                .first().key
        }

    override fun getGoalsShareByMedianCost(): Pair<Double, Double> {
        val totalGoals = players.sumOf { it.goals.toLong() }
        if (totalGoals == 0L) return 0.0 to 0.0

        val lower = sortedCosts[(sortedCosts.size - 1) / 2]
        val upper = sortedCosts[sortedCosts.size / 2]
        // Стоимости целые: достаточно округлить медиану вниз, не складывая два Long целиком.
        val medianFloor = lower / 2 + upper / 2 + Math.floorDiv(lower % 2 + upper % 2, 2L)
        if (medianFloor == Long.MAX_VALUE) return 1.0 to 0.0

        val moreExpensive = PlayerFilter.Not(PlayerFilter.CheaperThan(medianFloor + 1))
        val expensiveGoals = players.filter(moreExpensive::matches).sumOf { it.goals.toLong() }
        return (totalGoals - expensiveGoals).toDouble() / totalGoals to expensiveGoals.toDouble() / totalGoals
    }
}
