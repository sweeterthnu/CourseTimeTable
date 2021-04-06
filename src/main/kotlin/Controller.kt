import org.apache.poi.xssf.usermodel.*

class CellRange(
    val cells: MutableList<XSSFCell>
) {
    override fun toString() = cells.map { it.toString() }.joinToString { substring -> "\n$substring" }
}


class Controller(tableFile: XSSFWorkbook, indexSheet: Int) {
    private val table = tableFile.getSheetAt(indexSheet)

    private fun getTeachers(table: XSSFSheet): MutableList<Teacher> {
        val teachers: MutableList<Teacher> = mutableListOf()
        for (rowNumber in 0..table.lastRowNum) {
            val row = table.getRow(rowNumber) ?: continue
            val cell = row.getCell(3) ?: continue
            val isHeader = cell.toString().trim() == "ИЗВЕЩЕНИЕ"
            if (isHeader) {
                val teacherRowNumber = rowNumber + 4
                val teacherCell = table.getRow(teacherRowNumber)!!.getCell(1)!!
                val teacher = Teacher(
                    name = teacherCell.toString(),
                    timeTable = getTimeTable(teacherRowNumber, table)
                )
                teachers.add(teacher)
            }
        }
        return teachers
    }

    private fun getGroups(table: XSSFSheet): List<String> {
        val groups: MutableList<String> = mutableListOf()
        for (rowNumber in 0..table.lastRowNum) {
            val row = table.getRow(rowNumber) ?: continue
            for (columnNumber in 1..row.lastCellNum) {
                val cell = row.getCell(columnNumber) ?: continue
                val cellContent = cell.toString()
                var containsGroups = cellContent.any { it.isDigit() } && !cellContent.contains("расписание")
                if (containsGroups) {
                    val cellGroups = cellContent.split(" ").first().split("\\;|\\~".toRegex())
                    groups.addAll(cellGroups)
                }
            }
        }
        return groups.distinct()
    }

    private fun getClassrooms(table: XSSFSheet): List<String> {
        val classrooms: MutableList<String> = mutableListOf()
        for (rowNumber in 0..table.lastRowNum) {
            val row = table.getRow(rowNumber) ?: continue
            for (columnNumber in 1..row.lastCellNum) {
                val cell = row.getCell(columnNumber) ?: continue
                val cellContent = cell.toString()
                var containsClassroom = cellContent.any { it.isDigit() } && !cellContent.contains("расписание")
                if (containsClassroom) {
                    val classroom = cellContent.split(" ").last()
                    classrooms.add(classroom)
                }
            }
        }
        return classrooms.distinct()
    }

    private fun getCellRange(rowRangeIndex: Int, columnIndex: Int, rowRangeSize: Int, table: XSSFSheet): List<XSSFCell> {
        val cells: MutableList<XSSFCell> = mutableListOf()
        for (rowIndex in rowRangeIndex until rowRangeIndex + rowRangeSize) {
            val row = table.getRow(rowIndex)!!
            val cell = row.getCell(columnIndex)!!
            cells.add(cell)
        }
        return cells
    }


    private fun getTimeTable(teacherId: Int, table: XSSFSheet): TimeTable {
        val timeTable = TimeTable(mutableListOf(), mutableListOf())
        val rowRangeSize = 4
        var currentLesson: Lesson

        val firstRowRangeIndex = teacherId + 2
        val lastRowRangeIndex = (firstRowRangeIndex + (rowRangeSize * 5))
        var rowRangeIndex = firstRowRangeIndex

        var lastColumnIndex = 6

        // Проходим построчно
        while (rowRangeIndex < lastRowRangeIndex) {
            for (columnIndex in 1..lastColumnIndex) {
                val cellRange = getCellRange(rowRangeIndex, columnIndex, rowRangeSize, table)
                cellRange.map { it.toString().trim() }.zipWithNext().forEachIndexed { index, pair ->
                    if (pair.toList().all { it.isEmpty() }) {
                        val lesson = Lesson("","Окно")
                        when (index) {
                            0 -> timeTable.oddWeek.add(lesson)
                            2 -> timeTable.evenWeek.add(lesson)
                        }
                    } else if (pair.toList().all { it.isNotEmpty() } && pair.first.contains("\\d+".toRegex())) {
                        val lesson = Lesson(pair.first, pair.second)
                        when (index) {
                            0 -> timeTable.oddWeek.add(lesson)
                            1 -> {
                                timeTable.oddWeek.add(lesson)
                                timeTable.evenWeek.add(lesson)
                            }
                            2 -> timeTable.evenWeek.add(lesson)
                        }
                    }
                }
            }
            rowRangeIndex += rowRangeSize
        }
        return timeTable
    }

    fun printInfo() {
        val teachers = getTeachers(table)
        println(teachers)
    }
}