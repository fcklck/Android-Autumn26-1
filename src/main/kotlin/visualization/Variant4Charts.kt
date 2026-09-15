package visualization

import model.Player
import model.Team
import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartUtils
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.CategoryLabelPositions
import org.jfree.chart.axis.NumberAxis
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator
import org.jfree.chart.plot.DatasetRenderingOrder
import org.jfree.chart.plot.PlotOrientation
import org.jfree.chart.renderer.category.BarRenderer
import org.jfree.chart.renderer.category.LineAndShapeRenderer
import org.jfree.chart.renderer.category.StandardBarPainter
import org.jfree.chart.title.TextTitle
import org.jfree.chart.ui.RectangleInsets
import org.jfree.data.category.DefaultCategoryDataset
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Font
import java.io.File
import java.text.DecimalFormat

internal data class TeamValue(val team: Team, val cumulativePercent: Double)

internal fun countryShares(players: List<Player>): List<Pair<String, Double>> =
    players.groupingBy { it.nationality }.eachCount()
        .toList()
        .sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
        .map { (country, count) -> country to count * 100.0 / players.size }

internal fun topTeamValues(teams: List<Team>): List<TeamValue> {
    val total = teams.sumOf { it.totalTransferCost }
    val top = teams.sortedWith(
        compareByDescending<Team> { it.totalTransferCost }.thenBy { it.name }.thenBy { it.city }
    ).take(10)

    // Знаменатель — стоимость всех команд, даже если их больше десяти.
    return top.zip(top.runningFold(0L) { sum, team -> sum + team.totalTransferCost }.drop(1))
        .map { (team, sum) -> TeamValue(team, if (total == 0L) 0.0 else sum * 100.0 / total) }
}

object Variant4Charts {
    fun save(players: List<Player>, teams: List<Team>, outputDirectory: File): List<File> {
        outputDirectory.mkdirs()
        val countries = File(outputDirectory, "countries.png")
        val leaders = File(outputDirectory, "top-teams.png")
        ChartUtils.saveChartAsPNG(countries, countriesChart(players), 1250, 760)
        ChartUtils.saveChartAsPNG(leaders, teamsChart(teams), 1450, 900)
        return listOf(countries, leaders)
    }

    private fun countriesChart(players: List<Player>): JFreeChart {
        val dataset = DefaultCategoryDataset().apply {
            countryShares(players).forEach { (country, percent) ->
                addValue(percent, "Доля игроков", country)
            }
        }
        return ChartFactory.createBarChart(
            "Игроки по странам", "Страна", "Доля всех игроков, %", dataset,
            PlotOrientation.HORIZONTAL, false, true, false
        ).apply {
            style(this)
            addSubtitle(TextTitle("Всего игроков после обработки CSV: ${players.size}", textFont))
            categoryPlot.rangeAxis.upperMargin = 0.18
            (categoryPlot.renderer as BarRenderer).apply {
                setSeriesPaint(0, Color(226, 115, 170))
                setDefaultItemLabelGenerator(StandardCategoryItemLabelGenerator("{2}%", DecimalFormat("0.0")))
                setDefaultItemLabelsVisible(true)
                setDefaultItemLabelFont(textFont)
            }
        }
    }

    private fun teamsChart(teams: List<Team>): JFreeChart {
        val costs = DefaultCategoryDataset()
        val shares = DefaultCategoryDataset()
        topTeamValues(teams).forEach { (team, cumulativePercent) ->
            val label = "${team.name} (${team.city})"
            costs.addValue(team.totalTransferCost / 1_000_000.0, "Стоимость состава", label)
            shares.addValue(cumulativePercent, "Накопленная доля", label)
        }
        return ChartFactory.createBarChart(
            "Топ-10 команд по трансферной стоимости", "Команда", "Стоимость состава, млн", costs,
            PlotOrientation.VERTICAL, true, true, false
        ).apply {
            style(this)
            addSubtitle(TextTitle("Накопленная доля рассчитана от стоимости всех ${teams.size} команд", textFont))
            categoryPlot.apply {
                domainAxis.categoryLabelPositions = CategoryLabelPositions.UP_45
                domainAxis.maximumCategoryLabelWidthRatio = 2.0f
                rangeAxis.upperMargin = 0.18
                (renderer as BarRenderer).apply {
                    setSeriesPaint(0, Color(226, 115, 170))
                    setDefaultItemLabelGenerator(StandardCategoryItemLabelGenerator("{2}", DecimalFormat("0.0")))
                    setDefaultItemLabelsVisible(true)
                    setDefaultItemLabelFont(textFont)
                }
                setDataset(1, shares)
                setRangeAxis(1, NumberAxis("Накопленная доля общей стоимости, %").apply {
                    setRange(0.0, 105.0)
                    tickLabelFont = textFont
                    labelFont = textFont
                })
                mapDatasetToRangeAxis(1, 1)
                setRenderer(1, LineAndShapeRenderer(true, true).apply {
                    setSeriesPaint(0, Color(173, 54, 125))
                    setSeriesStroke(0, BasicStroke(3f))
                    setDefaultItemLabelGenerator(StandardCategoryItemLabelGenerator("{2}%", DecimalFormat("0.0")))
                    setDefaultItemLabelsVisible(true)
                    setDefaultItemLabelFont(textFont)
                })
                datasetRenderingOrder = DatasetRenderingOrder.FORWARD
            }
        }
    }

    private val textFont = Font(Font.SANS_SERIF, Font.PLAIN, 15)

    private fun style(chart: JFreeChart) {
        chart.backgroundPaint = Color.WHITE
        chart.padding = RectangleInsets(16.0, 16.0, 16.0, 16.0)
        chart.title.font = Font(Font.SANS_SERIF, Font.BOLD, 24)
        chart.legend?.itemFont = textFont
        chart.categoryPlot.apply {
            backgroundPaint = Color(248, 249, 251)
            rangeGridlinePaint = Color(215, 219, 224)
            outlinePaint = null
            domainAxis.tickLabelFont = textFont
            domainAxis.labelFont = textFont
            rangeAxis.tickLabelFont = textFont
            rangeAxis.labelFont = textFont
            (renderer as BarRenderer).apply {
                barPainter = StandardBarPainter()
                setShadowVisible(false)
                maximumBarWidth = 0.07
            }
        }
    }
}
