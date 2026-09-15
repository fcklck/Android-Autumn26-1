package model

data class Team(
    val name: String,
    val city: String,
    val players: List<Player> = emptyList(),
) {
    val totalTransferCost: Long by lazy { players.sumOf { it.transferCost } }
    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.map { it.redCards }.average()
    }
    val averageYellowCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.map { it.yellowCards }.average()
    }

    companion object {
        fun fromPlayers(players: List<Player>): List<Team> =
            players.groupBy { it.team.name to it.team.city }
                .map { (team, members) -> Team(team.first, team.second, members) }
    }
}
