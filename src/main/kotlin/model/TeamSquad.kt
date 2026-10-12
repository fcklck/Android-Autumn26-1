package model

data class TeamSquad(val team: Team, val players: List<Player>) {
    val totalTransferCost: Long by lazy { players.sumOf { it.transferCost } }
    val averageRedCards: Double by lazy {
        if (players.isEmpty()) 0.0 else players.map { it.redCards }.average()
    }

    companion object {
        fun fromPlayers(players: List<Player>): List<TeamSquad> =
            players.groupBy { it.team }
                .map { (team, members) -> TeamSquad(team, members) }
    }
}
