class Lesson(
    val weekDay: String,
    val timeInterval: String,
    val meta: String,
    val name: String,
    val teacher: Teacher
) {
    override fun toString(): String {
        return "\n$meta\n$name\n${teacher.name}\n\n"
    }
}