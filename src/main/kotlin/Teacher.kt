class Teacher(
    var name: String,
    var timeTable: TimeTable
) {
    override fun toString() = "\n$name\n$timeTable\n"
}