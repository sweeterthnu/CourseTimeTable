class TimeTable(
    val evenWeek: MutableList<Lesson>,
    val oddWeek: MutableList<Lesson>
) {
    override fun toString() = "$evenWeek\n$oddWeek\n"
}