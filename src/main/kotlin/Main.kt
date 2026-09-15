import parser.CsvParser
import report.report
import resolver.Resolver
import visualization.Variant4Charts
import java.io.File
import java.util.Locale

fun main(args: Array<String>) {
    val inputFile = args.firstOrNull()?.let(::File)
    val outputDirectory = File(args.getOrElse(1) { "build/reports/football" })
    fun openCsv() = inputFile?.bufferedReader()
        ?: checkNotNull(object {}.javaClass.getResourceAsStream("/fakePlayersDirty.csv")) {
            "Не найден ресурс fakePlayersDirty.csv"
        }.bufferedReader()

    val result = CsvParser.parse(openCsv())
    val players = result.players
    val resolver = Resolver(players)

    println(report {
        metric("Файл") { inputFile?.path ?: "fakePlayersDirty.csv" }
        metric("Прочитано игроков") { players.size }
        metric("Пропущено строк") { result.skippedCount }

        section("Запросы") {
            metric("1. Без агентства") { resolver.getCountWithoutAgency() }
            metric("2. Лучший бомбардир среди защитников") {
                if (players.none { it.position == model.Position.DEFENDER }) "Нет защитников"
                else resolver.getBestScorerDefender().let { (name, goals) -> "$name — $goals голов" }
            }
            metric("3. Позиция самого дорогого немецкого игрока") {
                if (players.none { it.nationality == "Germany" }) "Нет игроков из Германии"
                else resolver.getTheExpensiveGermanPlayerPosition()
            }
            metric("4. Команда с наибольшим средним числом красных карточек") {
                if (players.isEmpty()) "Нет игроков"
                else resolver.getTheRudestTeam().let { "${it.name} (${it.city}) — ${it.averageRedCards.decimal()}" }
            }
            section("5. Средняя трансферная стоимость по позициям") {
                resolver.getAverageTransferCostByPosition().forEach { (position, cost) ->
                    metric(position.russianName) { cost.decimal() }
                }
            }
            section("6. Топ-3 по голам и передачам") {
                resolver.getMostValuablePlayers().forEach { player ->
                    metric(player.name) { "${player.goals} + 2 × ${player.assists} = ${player.goals + 2L * player.assists}" }
                }
            }
            section("7. Самое популярное агентство по странам") {
                resolver.getMostPopularAgencyByCountry().forEach { (country, agency) ->
                    metric(country) { agency }
                }
            }
            section("8. Доли голов относительно медианы стоимости") {
                val (cheaper, moreExpensive) = resolver.getGoalsShareByMedianCost()
                metric("Не дороже медианы") { "${(cheaper * 100).decimal()}%" }
                metric("Дороже медианы") { "${(moreExpensive * 100).decimal()}%" }
            }
        }
        section("Дополнительное задание: потоковый поиск") {
            metric("Первый игрок дороже 50 млн, с 25+ матчами и без карточек") {
                CsvParser.findFirstExpensiveDisciplinedPlayer(openCsv())?.name ?: "Не найден"
            }
        }
    })

    Variant4Charts.save(players, resolver.teams, outputDirectory).forEach {
        println("График сохранён: ${it.path}")
    }
}

private fun Double.decimal() = String.format(Locale.forLanguageTag("ru-RU"), "%,.2f", this)
