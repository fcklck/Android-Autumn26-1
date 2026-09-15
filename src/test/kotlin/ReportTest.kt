import report.report
import kotlin.test.Test
import kotlin.test.assertEquals

class ReportTest {
    @Test
    fun `metrics and nested sections form a readable report`() {
        val players = listOf(2, 3)

        val result = report {
            metric("Игроков") { players.size }
            section("Атаки") {
                metric("Голов") { players.sum() }
                section("Среднее") {
                    metric("На игрока") { players.average() }
                }
            }
        }

        assertEquals("Игроков: 2\nАтаки:\n  Голов: 5\n  Среднее:\n    На игрока: 2.5", result)
    }
}
