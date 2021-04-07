import org.apache.poi.xssf.usermodel.*
import kotlinx.html.*
import kotlinx.html.table
import kotlinx.html.stream.*
import java.io.File

typealias Predicate<T> = (T) -> Boolean

class Controller(tableFile: XSSFWorkbook, sheetIndex: Int) {
    private val table = tableFile.getSheetAt(sheetIndex)
    private val weekDays = (1..6).map { table.getRow(7)!!.getCell(it).toString() }
    private val timeIntervals = (8 until 28 step 4).asIterable().map { table.getRow(it)!!.getCell(0).toString() }

    private fun getTeachers(): MutableList<Teacher> {
        val teachers: MutableList<Teacher> = mutableListOf()
        for (rowNumber in 0..table.lastRowNum) {
            val row = table.getRow(rowNumber) ?: continue
            val cell = row.getCell(3) ?: continue
            val isHeader = cell.toString().trim() == "ИЗВЕЩЕНИЕ"
            if (isHeader) {
                val teacherRowNumber = rowNumber + 4
                val teacherCell = table.getRow(teacherRowNumber)!!.getCell(1)!!
                val teacher = Teacher(
                    id = teacherRowNumber,
                    name = teacherCell.toString()
                )
                teachers.add(teacher)
            }
        }
        return teachers
    }

    private fun getGroups(): List<String> {
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

    private fun getCellRange(rowRangeIndex: Int, columnIndex: Int, rowRangeSize: Int): List<XSSFCell> {
        val cells: MutableList<XSSFCell> = mutableListOf()
        for (rowIndex in rowRangeIndex until rowRangeIndex + rowRangeSize) {
            val row = table.getRow(rowIndex)!!
            val cell = row.getCell(columnIndex)!!
            cells.add(cell)
        }
        return cells
    }

    private fun getSchedule(teacher: Teacher): Schedule {
        val rowRangeSize = 4
        val firstRowRangeIndex = teacher.id + 2
        val lastRowRangeIndex = (firstRowRangeIndex + (rowRangeSize * 5))
        val lastColumnIndex = 6

        val oddWeek: MutableList<Lesson> = mutableListOf()
        val evenWeek: MutableList<Lesson> = mutableListOf()

        for (rowRangeIndex in firstRowRangeIndex until lastRowRangeIndex step rowRangeSize) {
            for (columnIndex in 1..lastColumnIndex) {
                val cellRange = getCellRange(rowRangeIndex, columnIndex, rowRangeSize)
                cellRange.map { it.toString().trim() }.zipWithNext().forEachIndexed { index, pair ->
                    val weekDay = weekDays[columnIndex - 1]
                    val timeInterval = table.getRow(rowRangeIndex)!!.getCell(0).toString()
                    if (pair.toList().all { it.isNotEmpty() } && pair.first.contains("\\d+".toRegex())) {
                        val lesson = Lesson(weekDay, timeInterval, pair.first, pair.second, teacher)
                        when (index) {
                            0 -> oddWeek.add(lesson)
                            1 -> {
                                oddWeek.add(lesson)
                                evenWeek.add(lesson)
                            }
                            2 -> evenWeek.add(lesson)
                        }
                    }
                }
            }
        }
        return Schedule(oddWeek, evenWeek)
    }

    private fun getSchedule(predicate: Predicate<Lesson>): Schedule {
        val schedules = getTeachers().map { teacher -> getSchedule(teacher) }
        val oddWeekLessons = schedules.map { schedule ->
            schedule.oddWeek.filter(predicate)
        }.flatten().toMutableList()
        val evenWeekLessons = schedules.map { schedule ->
            schedule.evenWeek.filter(predicate)
        }.flatten().toMutableList()
        return Schedule(oddWeekLessons, evenWeekLessons)
    }

    fun writeScheduleIntoFile(weekType: WeekType, predicate: Predicate<Lesson>) {
        val schedule = getSchedule(predicate)
        val borderStyle = "border: 2px solid #095484;"
        File("Schedule.html").writeText(
            StringBuilder().appendHTML().html {
                body {
                    table {
                        style = borderStyle
                        tr {
                            th { +""}
                            weekDays.forEach { weekDay ->
                                th { +weekDay }
                            }
                        }
                        timeIntervals.forEach { timeInterval ->
                            tr {
                                td {
                                    +timeInterval
                                }
                                weekDays.forEach { weekDay ->
                                    val week = when (weekType) {
                                        WeekType.odd -> schedule.oddWeek
                                        WeekType.even -> schedule.evenWeek
                                    }
                                    val lesson = week.firstOrNull { lesson ->
                                        lesson.weekDay == weekDay && lesson.timeInterval == timeInterval
                                    } ?: Lesson(weekDay, timeInterval, "", "", Teacher.empty())
                                    td {
                                        style = borderStyle
                                        +lesson.toString()
                                    }
                                }
                            }
                        }
                    }
                }
            }.toString()
        )
    }
}