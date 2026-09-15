package parser

import model.Player
import model.Position
import model.Team
import java.io.Reader

data class ParseResult(val players: List<Player>, val skippedCount: Int)

object CsvParser {
    fun parseLine(line: String): Player? {
        val fields = line.split(';').map { it.trim() }
        if (fields.size != 12) return null

        return Player(
            name = fields[0].takeIf { it.isNotEmpty() } ?: return null,
            team = Team(
                name = fields[1].takeIf { it.isNotEmpty() } ?: return null,
                city = fields[2].takeIf { it.isNotEmpty() } ?: return null,
            ),
            position = Position.fromCsvTokenOrNull(fields[3]) ?: return null,
            nationality = fields[4].takeIf { it.isNotEmpty() } ?: return null,
            agency = fields[5].takeIf { it.isNotEmpty() },
            transferCost = fields[6].toLongOrNull() ?: return null,
            participations = fields[7].toIntOrNull() ?: return null,
            goals = fields[8].toIntOrNull() ?: return null,
            assists = fields[9].toIntOrNull() ?: return null,
            yellowCards = fields[10].toIntOrNull() ?: return null,
            redCards = fields[11].toIntOrNull() ?: return null,
        )
    }

    fun parse(lines: Sequence<String>): ParseResult =
        lines.drop(1).map(::parseLine).toList().let { players ->
            ParseResult(players.filterNotNull(), players.count { it == null })
        }

    fun parse(reader: Reader): ParseResult = reader.buffered().useLines { parse(it) }

    fun findFirstExpensiveDisciplinedPlayer(reader: Reader): Player? =
        reader.buffered().useLines { lines ->
            lines.drop(1).mapNotNull(::parseLine).firstOrNull {
                it.transferCost > 50_000_000 && it.participations >= 25 &&
                    it.yellowCards == 0 && it.redCards == 0
            }
        }
}
