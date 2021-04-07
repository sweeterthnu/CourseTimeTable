class Schedule(
    val oddWeek: List<Lesson>,
    val evenWeek: List<Lesson>
) {
    override fun toString() = "$evenWeek\n$oddWeek\n"
}