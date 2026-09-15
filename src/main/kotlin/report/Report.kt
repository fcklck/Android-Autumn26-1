package report

fun report(block: ReportBuilder.() -> Unit): String =
    ReportBuilder().apply(block).toString()

class ReportBuilder(private val indent: String = "") {
    private val lines = mutableListOf<String>()

    fun metric(name: String, value: () -> Any?) {
        lines += "$indent$name: ${value()}"
    }

    fun section(name: String, block: ReportBuilder.() -> Unit) {
        lines += "$indent$name:"
        lines += ReportBuilder("$indent  ").apply(block).lines
    }

    override fun toString(): String = lines.joinToString("\n")
}
