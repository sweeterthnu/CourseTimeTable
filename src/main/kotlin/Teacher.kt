class Teacher(
    val id: Int,
    val name: String
) {
    override fun toString() = "\n$id\n$name"

    companion object Factory {
        fun empty(): Teacher = Teacher(-1, "")
    }
}