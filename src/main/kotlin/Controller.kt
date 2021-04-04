import org.apache.poi.xssf.usermodel.*

class Controller(tableFile: XSSFWorkbook, indexSheet: Int) {
    private val table = tableFile.getSheetAt(indexSheet)

    private fun getTeachers(table: XSSFSheet): MutableList<Teacher> {
        val arrayTeachers: MutableList<Teacher> = mutableListOf()
        var currentTeacher: Teacher

        var i = 0
        while (table.getRow(32 * i + 6)?.getCell(1) != null) {
            currentTeacher = Teacher(name = table.getRow(32 * i + 6).getCell(1).toString(), timeTable = getTimeForOneTeacher(i,table))
            arrayTeachers.add(currentTeacher)
            i++
        }

        return arrayTeachers
    }
    private fun getTimeForOneTeacher(idTeacher: Int, table: XSSFSheet): TimeTable
    {
        val timeTable= TimeTable(mutableListOf(), mutableListOf())
        var currentLesson: Lesson

        val startRow = 32 * idTeacher + 8

        var currentRow = startRow
        var currentCell = 1

        val lastRow = currentRow + 5 * 4
        val lastCell = 6

        while (currentCell <= lastCell)
        {
            while (currentRow < lastRow)
            {
                if (table.getRow(currentRow).getCell(currentCell).toString() == "" || table.getRow(currentRow).getCell(currentCell).toString() == "            ")
                {
                    currentLesson = Lesson("","Окно")
                    currentRow++
                }
                else
                {
                    currentLesson = Lesson(
                        table.getRow(currentRow).getCell(currentCell).toString(),
                        table.getRow(++currentRow).getCell(currentCell).toString())
                }
                if(currentRow%4-1 == 0)
                    timeTable.oddWeek.add(currentLesson)
                else
                    timeTable.evenWeek.add(currentLesson)
                currentRow++
            }

            currentCell++
            currentRow = startRow
        }

        return timeTable
    }

    fun printInfo()
    {
        val teachers = getTeachers(table)

        println(teachers)
    }
}